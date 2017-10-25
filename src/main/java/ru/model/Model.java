package ru.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "model")
public class Model implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;
    @Column(name = "model_name", nullable = true)
    private String modelName;
    @Column(name = "description")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model",orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @OrderBy(value = "gvi")
    private List<GVIBase> gviBaseList;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model",orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @OrderBy(value = "ckc")
    private List<CKCBase> ckcBaseList;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model",orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @OrderBy(value = "freqtrn")
    private List<FreqTrnsBase> freqTrnsBaseList;

    public List<GVIBase> getGviBaseList() {
        return gviBaseList;
    }

    public void setGviBaseList(List<GVIBase> gviBaseList) {
        this.gviBaseList = gviBaseList;
    }

    public List<CKCBase> getCkcBaseList() {
        return ckcBaseList;
    }

    public void setCkcBaseList(List<CKCBase> ckcBaseList) {
        this.ckcBaseList = ckcBaseList;
    }

    public List<FreqTrnsBase> getFreqTrnsBaseList() {
        return freqTrnsBaseList;
    }

    public void setFreqTrnsBaseList(List<FreqTrnsBase> freqTrnsBaseList) {
        this.freqTrnsBaseList = freqTrnsBaseList;
    }

    public Model(String modelName, String description) {

        this.modelName = modelName;
        this.description = description;
    }


    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Model() {
    }

    public Model(Long modelId) {
        this.modelId = modelId;
    }

    @Override
    public String toString() {
        return modelId + " : " + modelName + " : " + description;
    }

}
