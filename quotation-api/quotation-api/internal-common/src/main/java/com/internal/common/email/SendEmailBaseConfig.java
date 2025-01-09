package com.internal.common.email;

import com.internal.common.core.domain.dto.EmailModelInfoConvertDTO;
import com.internal.common.core.domain.dto.EmailSubjectInfoConvertDTO;
import com.internal.common.core.domain.dto.EmailUserInfoConvertDTO;
import com.internal.common.core.domain.entity.SysUser;
import com.internal.common.utils.TemplateUtil;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

public class SendEmailBaseConfig {

    private String flag;    //smtp是否需要认证
    private String host;    //邮件服务器主机名
    private String username;    //用户名
    private String password;    //密码
    private String title;   //标题
    private String content; //发送的内容
    private Boolean listFlag;   //是否发送复杂内容
    private Boolean cc; //是否抄送
    private List<SysUser> copyUser;  //抄送人
    private Address[] copyAddress;  //报错后 抄送人
    private Boolean to; //是否发送
    private List<SysUser> receiveUser;   //发送人
    private Address[] receiveAddress;   //报错后 发送人
    private EmailModelInfoConvertDTO emailModelInfoConvertDTO; // 邮箱模版占位符转换信息
    private EmailSubjectInfoConvertDTO emSubjectInfoConvertDTO; // 邮箱主题占位符转换信息




    //如果其他邮箱验证 在sendEmailToAllUser()，添加if判断


    public SendEmailBaseConfig(String flag, String host, String username, String password, String title, String content, Boolean listFlag,
                               Boolean cc, List<SysUser> copyUser, Address[] copyAddress, Boolean to, List<SysUser> receiveUser, Address[] receiveAddress,
                               EmailModelInfoConvertDTO emailModelInfoConvertDTO, EmailSubjectInfoConvertDTO emSubjectInfoConvertDTO) {
        this.flag = flag;
        this.host = host;
        this.username = username;
        this.password = password;
        this.title = title;
        this.content = content;
        this.listFlag = listFlag;
        this.cc = cc;
        this.copyUser = copyUser;
        this.copyAddress = copyAddress;
        this.to = to;
        this.receiveUser = receiveUser;
        this.receiveAddress = receiveAddress;
        this.emailModelInfoConvertDTO = emailModelInfoConvertDTO;
        this.emSubjectInfoConvertDTO = emSubjectInfoConvertDTO;
    }

    //每天定时和crm同步项目名称任务提醒邮件
    public void sendEmailToSyncProjectManager(String contentStr) throws MessagingException, GeneralSecurityException {
        sendEmailNTES(contentStr);
    }


    //每天向所有用户发送邮件
    public void sendEmailToAllUser() throws GeneralSecurityException, MessagingException {
        String[] split = username.split("@");
        //配置发送邮件的属性
        //Properties properties = new Properties();
        if ("qq.com".equals(split[1])) {
            sendEmailQQ();
            return;
        }

        if ("163.com".equals(split[1])) {
            sendEmailNTES(sendEmailContentStr());
            return;
        }
        sendEmailNTES(sendEmailContentStr());
    }

    //初始化配置数据
    private Properties initProperties() {
        return new Properties();
    }


    //通过QQ邮箱发送
    private void sendEmailQQ() throws GeneralSecurityException, MessagingException {
        Properties properties = initProperties();
        properties.setProperty("mail.host", host); //设置协议主机
        properties.setProperty("mail.transport.protocol", "smtp"); // 邮件发送协议
        properties.setProperty("mail.smtp.auth", flag); //设置smtp是否需要认证
        // 关于QQ邮箱，还要设置SSL加密，加上以下代码即可
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //发件人邮件用户名、授权码
                return new PasswordAuthentication(username, password);
            }
        });

        //设置会话是debug模式(会打印更多相关信息,生产环境可设为false)
        session.setDebug(true);

        //获取发送器对象:提供指定的协议
        //Transport transport = session.getTransport("smtp");
        Transport transport = session.getTransport();

        //设置发件人的信息
        transport.connect(host, username, password);

        //创建邮件主题信息对象
        MimeMessage message = new MimeMessage(session);

        //设置发件人
        message.setFrom(new InternetAddress(username));

        //设置邮件主题
        title = title + "!";
        try {
            message.setSubject(new String(title.getBytes(StandardCharsets.UTF_8), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //设置邮件正文
        //message.setText(content);
        if (listFlag) {
            content = sendEmailContentStr();
        }
        message.setContent(content, "text/html;charset=UTF-8");

        // 设置发信时间
        message.setSentDate(new Date());

        //抄送
        if (cc) {
            if (copyUser.size() > 0) {
                //message.setRecipient(Message.RecipientType.CC,new InternetAddress("email地址"));
                message.setRecipients(Message.RecipientType.CC, ccAddresses(copyUser));
            }
        }
        if (to) {
            message.setRecipient(Message.RecipientType.CC, new InternetAddress(username));
            if (receiveUser.size() > 0) {
                message.setRecipients(Message.RecipientType.TO, toAddresses(receiveUser));
            }
        }


        //发送邮件
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (SendFailedException e) {
            //得到有效但未能成功将消息发送到的地址
            exceptionAddress(e.getValidUnsentAddresses());
        }
        //关闭资源
        transport.close();
    }

    //通过网易邮箱发送
    private void sendEmailNTES(String contentStr) throws MessagingException, GeneralSecurityException {
        Properties properties = initProperties();
        //设置发送的协议
        //properties.setProperty("mail.transport.protocol", "SMTP");

        //设置发送邮件的服务器
        //properties.setProperty("mail.host", host);
        //properties.setProperty("mail.smtp.auth", flag);// 指定验证为true

        properties.setProperty("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", 25);
        // 发件人的账号
        properties.put("mail.user", username);
        // 访问SMTP服务时需要提供的密码
        properties.put("mail.password", password);

        // 创建验证器
        Authenticator auth = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //设置发送人的帐号和密码
                return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getInstance(properties, auth);

        // 2.创建一个Message，它相当于是邮件内容
        Message message = new MimeMessage(session);

        //设置发送者
        message.setFrom(new InternetAddress(username));

        //设置发送方式与接收者
        //抄送
        if (cc) {
            if (copyUser.size() > 0) {
                message.setRecipients(Message.RecipientType.CC, ccAddresses(copyUser));
            }
        }
        if (to) {
            if (receiveUser.size() > 0) {
                message.setRecipients(Message.RecipientType.TO, toAddresses(receiveUser));
            }
        }

        //设置邮件主题
        try {
            String encodedSubject = MimeUtility.encodeText(title, "UTF-8", null);;
            message.setSubject(encodedSubject);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (listFlag) {
            content = contentStr;
        }
        //设置邮件内容
        message.setContent(content, "text/html;charset=utf-8");

        // 3.创建 Transport用于将邮件发送
        Transport transport = session.getTransport();

        //设置发件人的信息
        transport.connect(host, username, password);
        //发送邮件
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (SendFailedException e) {
            //得到有效但未能成功将消息发送到的地址
            exceptionAddress(e.getValidUnsentAddresses());
        }
        //关闭资源
        transport.close();
        //Transport.send(message);
    }

    //设置邮件内容，模版转换
    private String sendEmailContentStr() {
        title = TemplateUtil.replaceTemplate(title, emSubjectInfoConvertDTO);
        String replaceTemplate = TemplateUtil.replaceTemplate(content, emailModelInfoConvertDTO);
        // 替换所有换行符为 <br/> TODO: 没起作用
        replaceTemplate = replaceTemplate.replaceAll("\r?\n", "<br/>");
        return replaceTemplate;
    }

    //设置抄送人地址
    private Address[] ccAddresses(List<SysUser> copyUser) throws AddressException {
        // 使用 Set 来跟踪唯一的电子邮件地址
        Set<String> uniqueEmails = new HashSet<>();
        // 使用 List 来存储有效的 InternetAddress
        List<Address> addressesList = new ArrayList<>();

        for (SysUser user : copyUser) {
            String email = user.getEmail();
            // 避免空值和重复
            if (email != null && uniqueEmails.add(email)) {
                addressesList.add(new InternetAddress(email));
            }
        }
        // 将 List 转换为数组并返回
        return addressesList.toArray(new Address[0]);
    }

    //设置发送人地址
    private Address[] toAddresses(List<SysUser> receiveUser) throws AddressException {
        // 使用 Set 来跟踪唯一的电子邮件地址
        Set<String> uniqueEmails = new HashSet<>();
        // 使用 List 来临时存储有效的 InternetAddress
        List<Address> addressesList = new ArrayList<>();
        for (SysUser user : receiveUser) {
            String email = user.getEmail();
            // 避免空值和重复
            if (email != null && uniqueEmails.add(email)) {
                addressesList.add(new InternetAddress(email));
            }
        }
        // 将 List 转换为数组并返回
        return addressesList.toArray(new Address[0]);
    }

    //接受者邮箱不正确，过滤后得到正确的邮箱
    private void exceptionAddress(Address[] addresses) throws MessagingException, GeneralSecurityException {
        if (addresses.length > 0) {
            Address[] address = new InternetAddress[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                address[i] = new InternetAddress(addresses[i].toString());
            }
            receiveAddress = address;
            sendMailWhenException();
        }
    }

    //给正确的邮箱发送邮件
    private void sendMailWhenException() throws GeneralSecurityException, MessagingException {
        String[] split = username.split("@");
        //配置发送邮件的属性
        Properties properties = new Properties();
        if ("qq.com".equals(split[1])) {
            sendMailQQWhenException();
            return;
        }

        if ("163.com".equals(split[1])) {
            sendMailNTESWhenException();
        }
    }

    //正确的QQ邮箱发邮件
    private void sendMailQQWhenException() throws GeneralSecurityException, MessagingException {
        Properties properties = initProperties();
        properties.setProperty("mail.host", host); //设置协议主机
        properties.setProperty("mail.transport.protocol", "smtp"); // 邮件发送协议
        properties.setProperty("mail.smtp.auth", flag); //设置smtp是否需要认证
        // 关于QQ邮箱，还要设置SSL加密，加上以下代码即可
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //发件人邮件用户名、授权码
                return new PasswordAuthentication(username, password);
            }
        });

        //设置会话是debug模式(会打印更多相关信息,生产环境可设为false)
        session.setDebug(true);

        //获取发送器对象:提供指定的协议
        //Transport transport = session.getTransport("smtp");
        Transport transport = session.getTransport();

        //设置发件人的信息
        transport.connect(host, username, password);

        //创建邮件主题信息对象
        MimeMessage message = new MimeMessage(session);

        //设置发件人
        message.setFrom(new InternetAddress(username));

        //设置邮件主题
        try {
            message.setSubject(new String(title.getBytes(StandardCharsets.UTF_8), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //设置邮件正文
        //message.setText(content);
        if (listFlag) {
            content = sendEmailContentStr();
        }
        message.setContent(content, "text/html;charset=UTF-8");

        // 设置发信时间
        message.setSentDate(new Date());

        //抄送
        if (cc) {
            if (copyAddress.length > 0) {
                message.setRecipients(Message.RecipientType.CC, copyAddress);
            }
        }
        if (to) {
            if (receiveAddress.length > 0) {
                message.setRecipients(Message.RecipientType.TO, receiveAddress);
            }
        }


        //发送邮件
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } catch (SendFailedException e) {
            //得到有效但未能成功将消息发送到的地址
            exceptionAddress(e.getValidUnsentAddresses());
        }
        //关闭资源
        transport.close();
    }

    //正确的网易邮箱发数据
    private void sendMailNTESWhenException() throws MessagingException {
        Properties properties = initProperties();
        //设置发送的协议
        //properties.setProperty("mail.transport.protocol", "SMTP");

        //设置发送邮件的服务器
        //properties.setProperty("mail.host", host);
        //properties.setProperty("mail.smtp.auth", flag);// 指定验证为true

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", 25);
        // 发件人的账号
        properties.put("mail.user", username);
        // 访问SMTP服务时需要提供的密码
        properties.put("mail.password", password);

        // 创建验证器
        Authenticator auth = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //设置发送人的帐号和密码
                return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getInstance(properties, auth);

        // 2.创建一个Message，它相当于是邮件内容
        Message message = new MimeMessage(session);

        //设置发送者
        message.setFrom(new InternetAddress(username));

        //设置发送方式与接收者
        //抄送
        if (cc) {
            if (copyAddress.length > 0) {
                message.setRecipients(Message.RecipientType.CC, copyAddress);
            }
        }
        if (to) {
            if (receiveAddress.length > 0) {
                message.setRecipients(Message.RecipientType.TO, receiveAddress);
            }
        }

        //设置邮件主题
        title = title + "!";
        try {
            message.setSubject(new String(title.getBytes(StandardCharsets.UTF_8), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (listFlag) {
            content = sendEmailContentStr();
        }
        //设置邮件内容
        message.setContent(content, "text/html;charset=utf-8");

        // 3.创建 Transport用于将邮件发送
        Transport.send(message);
    }

}
