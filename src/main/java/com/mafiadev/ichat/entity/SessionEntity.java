package com.mafiadev.ichat.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(name = "TIPS")
    String tips;
    @Column(name = "STRICT")
    Boolean strict;
    @Column(name = "GPT4_MODEL")
    String gpt4Model;
}
