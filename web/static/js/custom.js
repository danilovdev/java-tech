$(document).ready(function() {
    $('#sendToTelegramBtn').click(function() {
        var email = $('#email').val();
        var name = $('#name').val();
        var message = $('#message').val();

            $.ajax({
            type: "POST",
            url: "/MailSendingServlet",
            data: { email: email, name: name,  message: message}
        });
    });
});