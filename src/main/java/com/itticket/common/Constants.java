package com.itticket.common;

public class Constants {

    public static final String TICKET_PREFIX = "TICKET";
    
    public static final String FILE_UPLOAD_DIR = "./uploads/";
    
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    public static final String[] ALLOWED_FILE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf", ".docx", ".xlsx"};
    
    public static final long SUSPEND_MAX_DURATION = 72 * 60 * 60 * 1000L;
    
    public static final String JWT_TOKEN_HEADER = "Authorization";
    
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    
    public static final int MAX_URGENCY = 3;
    
    public static final int MAX_IMPACT_SCOPE = 3;
}