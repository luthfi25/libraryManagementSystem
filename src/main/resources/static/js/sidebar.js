$(function() {
  console.log(window.location.pathname);
  switch (window.location.pathname) {
      case '/home':
          $('#allBooks').addClass('active');
          break;
      case '/admin/users/':
          $('#allusers').addClass('active');
          break;
      case '/admin/users/create':
          $('#newUser').addClass('active');
          break;
      case '/admin/books/create':
        $('#newBook').addClass('active');
        break;
      case '/admin/users/search':
        $('#searchUsers').addClass('active');
        break;
      default:
          break;
  }
});