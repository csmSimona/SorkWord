package com.mingrisoft.greendao.entity.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

@Entity
public class CET4Entity  implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String word;
    private String english;
    private String china;
    private String sign;
    @Generated(hash = 1296110788)
    public CET4Entity(Long id, String word, String english, String china,
                      String sign) {
        this.id = id;
        this.word = word;
        this.english = english;
        this.china = china;
        this.sign = sign;
    }
    @Generated(hash = 413105732)
    public CET4Entity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getWord() {
        return this.word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getEnglish() {
        return this.english;
    }
    public void setEnglish(String english) {
        this.english = english;
    }
    public String getChina() {
        return this.china;
    }
    public void setChina(String china) {
        this.china = china;
    }
    public String getSign() {
        return this.sign;
    }
    public void setSign(String sign) {
        this.sign = sign;
    }

}
