package kz.smarthealth.userservice.util;

public enum MessageSource {

    INVALID_ROLES("Invalid roles provided."),
    EMAIL_IN_USE("%s is already in use, please provide another email address."),
    USER_BY_ID_NOT_FOUND("User with id=%s not found."),
    USER_BY_EMAIL_NOT_FOUND("User with email=%s not found."),
    ROLE_BY_NAME_NOT_FOUND("Invalid role provided: %s"),
    INVALID_PROFILE_PICTURE_FILE_EXTENSION("Invalid file extension");

    private String text;

    MessageSource(String text) {
        this.text = text;
    }

    public String getText(String... params) {
        return String.format(this.text, params);
    }
}
