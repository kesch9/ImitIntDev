package ru.model;

import javax.persistence.*;
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model")
    @OrderBy(value = "gvi")
    private List<GVIBase> gviBaseList;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model")
    @OrderBy(value = "ckc")
    private List<CKCBase> ckcBaseList;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "model")
    @OrderBy(value = "freqtrn")
    private List<FreqTrnsBase> freqTrnsBaseList;

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

    @Override
    public String toString() {
        return modelId + " : " + modelName + " : " + description;
    }

}
