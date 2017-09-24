package com.danilovdev.mail;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.urlfetch.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by alexeydanilov on 24/09/2017.
 */

public class MailSendingServlet extends HttpServlet {

    private MailService mailService = MailServiceFactory.getMailService();

    private static final HTTPHeader CONTENT_TYPE_HEADER =
            new HTTPHeader("Content-Type", "application/x-www-form-urlencoded");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.sendToTelegramBotWithUrlFetch(request);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.sendToTelegramBotWithUrlFetch(request);
    }

    private void sendSimpleMail(HttpServletRequest request) throws ServletException, IOException  {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@admin.com", "COMPANY_TITLE"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("admin@admin.com", "Mr. User"));
            msg.setSubject("New email from COMPANY_TITLE received");
            msg.setText(this.getMessageFromRequest(request));
        } catch (AddressException e) {
            // ...
        } catch (MessagingException e) {
            // ...
        } catch (UnsupportedEncodingException e) {
            // ...
        }
    }

    private void sendEmailToAdmins(HttpServletRequest request) {
        try {
            MailService.Message msg = new MailService.Message();
            msg.setSender("admin@admin.com");
            msg.setSubject("New email from COMPANY_TITLE received");
            msg.setTextBody(this.getMessageFromRequest(request));
            mailService.sendToAdmins(msg);
        } catch (Throwable t) {
            // ...
        }
    }

    private String getMessageFromRequest(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        ArrayList<String> parameterList = new ArrayList();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);

            StringBuilder builder = new StringBuilder();
            for(String s : paramValues) {
                builder.append(s);
            }
            builder.append("\n");
            String paramValue = builder.toString();
            parameterList.add(paramValue);
        }

        StringBuilder builder = new StringBuilder();
        for (String param: parameterList) {
            builder.append(param);
        }

        String message = builder.toString();
        return message;
    }

    private void sendToTelegramBotWithUrlFetch(HttpServletRequest request) throws IOException {
        String botToken = "BOT_ID";
        String groupId = "-GROUP_ID";
        String message = this.getMessageFromRequest(request);
        URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        String botUrlStr = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        URL botUrl = new URL(botUrlStr);
        HTTPRequest httpRequest = new HTTPRequest(botUrl, HTTPMethod.POST);

        Map<String, String> map = new LinkedHashMap<>();

        map.put("chat_id", encode(groupId, true));
        map.put("text", encode(message, true));

        httpRequest.addHeader(CONTENT_TYPE_HEADER);
        httpRequest.setPayload(getPostData(map));

        HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);
        if (httpResponse.getResponseCode() != 200) {
            throw new IOException(new String(httpResponse.getContent()));
        }
    }

    private static byte[] getPostData(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove the trailing &.
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String encode(String value, boolean required)
            throws UnsupportedEncodingException {
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException("Required parameter not set.");
            }
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }
}

