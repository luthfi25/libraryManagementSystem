package com.ood.libraryManagementSystem.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ood.libraryManagementSystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        typeReference = new TypeReference<List<User>>(){};
        classLoader = getClass().getClassLoader();
    }

    public BCryptPasswordEncoder getbCryptPasswordEncoder() {
        return bCryptPasswordEncoder;
    }

    public List<User> allUser() {
        inputStream = TypeReference.class.getResourceAsStream("/json/users.json");
        List<User> users = new ArrayList<>();

        try {
            users = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            System.out.println("Failed reading users " + e.toString());
            return null;
        }

        return users;
    }

    public void writeToDatabase(List<User> users) {
        File databaseFile = new File(classLoader.getResource("json/users.json").getFile());

        try {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(databaseFile, JsonEncoding.UTF8);
            mapper.writeValue(generator, users);
            generator.flush();
            generator.close();
        } catch (IOException e) {
            System.out.println("Failed writing users " + e.toString());
        }
    }

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

    public void saveUser(User user) {
        List<User> users = allUser();

        if (user == null) {
            return;
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setId(users.size());
        users.add(user);
        writeToDatabase(users);
    }

    public boolean updateUser(User user) {
        List<User> users = allUser();

        if (user == null) {
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
                return false;
            }
        }

        writeToDatabase(users);
        return true;
    }

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

        writeToDatabase(users);
    }

}
