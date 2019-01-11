package com.ood.libraryManagementSystem.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ood.libraryManagementSystem.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service("userService")
public class UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ObjectMapper mapper;
    private TypeReference<List<User>> typeReference;
    private InputStream inputStream;
    private ClassLoader classLoader;
    private KeyService keyService;
    private boolean isFileLocked = false;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        typeReference = new TypeReference<List<User>>(){};
        classLoader = getClass().getClassLoader();
        keyService = new KeyService();
    }

    public List<User> encryptUsers(List<User> users) {
        for (User u: users) {
            u.setUsername(keyService.encrypt(u.getUsername()));
            u.setName(keyService.encrypt(u.getName()));
            u.setPassword(keyService.encrypt(u.getPassword()));
            u.setRole(keyService.encrypt(u.getRole()));
        }

        return users;
    }

    public List<User> decryptUsers(List<User> users) {
        for (User u: users) {
            u.setUsername(keyService.decrypt(u.getUsername()));
            u.setName(keyService.decrypt(u.getName()));
            u.setPassword(keyService.decrypt(u.getPassword()));
            u.setRole(keyService.decrypt(u.getRole()));
        }

        return users;
    }

    public BCryptPasswordEncoder getbCryptPasswordEncoder() {
        return bCryptPasswordEncoder;
    }

//    @Async
    public List<User> allUser() {
        inputStream = TypeReference.class.getResourceAsStream("/json/users.json");
        List<User> users = new ArrayList<>();

        try {
            users = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            logger.error("Failed reading users " + e.toString());
            return null;
        }

        users = decryptUsers(users);
        return users;
    }

    public boolean writeToDatabase(List<User> users) {
        if (isFileLocked) {
            return false;
        }

        isFileLocked = true;
        users = encryptUsers(users);

        try {
            ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
            File databaseFile = appContext.getResource("classpath:json/users.json").getFile();

            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(databaseFile, JsonEncoding.UTF8);
            mapper.writeValue(generator, users);
            generator.flush();
            generator.close();
        } catch (IOException e) {
            logger.error("Failed writing users " + e.toString());
        } finally {
            isFileLocked = false;
        }

        return true;
    }

//    @Async
    public User findUserByUsername(String username) {
        List<User> users = allUser();

        if (users != null) {
            List<User> desiredUser = users.stream().filter(u -> u.getUsername() != null && u.getUsername().equals(username)).collect(Collectors.toList());

            if (desiredUser.size() > 0) {
                return desiredUser.get(0);
            }
        }

        return null;
    }

//    @Async
    public void saveUser(User user) {
        List<User> users = allUser();

        if (user == null) {
            logger.error("failed creating new user, user data is null");
            return;
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setId(users.size());
        users.add(user);

        boolean succeedWrite = writeToDatabase(users);
        if (!succeedWrite) {
            logger.error("failed writing user, file is locked");
            return;
        }
    }

//    @Async
    public boolean updateUser(User user) {
        List<User> users = allUser();

        if (users == null) {
            return false;
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        users.set(user.getId(), user);

        //CHECK DUPLICATE
        Set<String> usernames = new HashSet<String>();
        for (User u : users) {
            String un = u.getUsername();
            if (!usernames.contains(un)){
                usernames.add(un);
            } else {
                //DUPLICATE
                logger.error("failed updating user, duplicate username. username: " + user.getUsername());
                return false;
            }
        }

        boolean succeedWrite = writeToDatabase(users);
        if (!succeedWrite) {
            logger.error("failed writing user, file is locked");
            return false;
        }

        return true;
    }

//    @Async
    public void deleteUser(String username) {
        List<User> users = allUser();

        if (users == null) {
            return;
        }

        users.removeIf(u -> u.getUsername().equals(username));

        //RECOMPILE ID
        int i = 0;
        for (User u:users) {
            u.setId(i);
            i++;
        }

        logger.info("sucessfully deleted user with username: " + username);

        boolean succeedWrite = writeToDatabase(users);
        if (!succeedWrite) {
            logger.error("failed writing user, file is locked");
            return;
        }
    }

}
