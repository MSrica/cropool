package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class RegisterReq {
    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    @SerializedName("e_mail")
    private final String email;

    @SerializedName("password")
    private final String password;

    @SerializedName("registration_id")
    private final String registrationId;

    public RegisterReq(String firstName, String lastName, String email, String password, String registrationId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.registrationId = registrationId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRegistrationId() {
        return registrationId;
    }


    @Override
    public String toString() {
        return "RegisterReq{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", registrationId='" + registrationId + '\'' +
                '}';
    }
}
