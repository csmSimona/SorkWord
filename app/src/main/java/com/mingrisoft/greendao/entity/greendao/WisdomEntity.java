package com.mingrisoft.greendao.entity.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

@Entity
public class WisdomEntity {
    @Id
    private Long id;
    private String english;
    private String china;
    @Generated(hash = 75080817)
    public WisdomEntity(Long id, String english, String china) {
        this.id = id;
        this.english = english;
        this.china = china;
    }
    @Generated(hash = 722270434)
    public WisdomEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
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
}
