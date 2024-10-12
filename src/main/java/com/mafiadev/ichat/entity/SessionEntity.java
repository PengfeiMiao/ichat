package com.mafiadev.ichat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SESSION")
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    Integer id;
    @Column(name = "USER_NAME")
    String userName;
    @Column(name = "LOGIN")
    Boolean login;
    @Column(name = "CHAT_MODEL")
    String chatModel;
    @Column(name = "IMAGE_MODEL")
    String imageModel;
    @Column(name = "TOOL_MODEL")
    String toolModel;
    @Column(name = "TIPS")
    String tips;
    @Column(name = "STRICT")
    Boolean strict;
}
