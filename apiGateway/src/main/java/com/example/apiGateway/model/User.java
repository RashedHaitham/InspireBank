package com.example.apiGateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
@Entry(base = "ou=users", objectClasses = { "inetOrgPerson", "top" })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "uid")
    private String username;

    @Attribute(name = "cn")
    private String fullName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "mail")
    private String email;

    private String role;

    @Attribute(name = "userPassword")
    private String password;
}
