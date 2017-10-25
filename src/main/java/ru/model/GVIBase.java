package ru.model;

import javax.persistence.*;

@Entity
@Table(name = "GVI")
public class GVIBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gvi_id")
    private Long gviId;
    @Column(name = "kod")
    private String kod;
    @Column(name = "name")
    private String name;
    @Column(name = "val")
    private Integer value;
    @Column(name = "unit")
    private String unit;
    @Column(name = "type")
    private String type;
    @Column(name = "vw")
    private String view;
    @Column(name = "description")
    private String description;
    @Column(name = "adres")
    private Integer adres;
    @Column(name = "wrt")
    private Integer write;
    @Column(name = "min1")
    private Integer min;
    @Column(name = "max1")
    private Integer max;
    @Column(name = "def")
    private Integer def;
    @Column(name = "koef")
    private Integer koef;
    @Column(name = "size1")
    private Integer size;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, targetEntity = Model.class)
    @JoinColumn(name = "model_id")
    private Model model;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Integer getKoef() {
        return koef;
    }

    public void setKoef(Integer koef) {
        this.koef = koef;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setAdres(Integer adres) {
        this.adres = adres;
    }

    public void setWrite(Integer write) {
        this.write = write;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public void setDef(Integer def) {
        this.def = def;
    }

    public int getDef() {
        return def;
    }

    public String getKod() {
        return kod;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getType() {
        return type;
    }

    public String getView() {
        return view;
    }

    public Integer getAdres() {
        return adres;
    }

    public Integer getWrite() {
        return write;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public Long getGviId() {
        return gviId;
    }

    public void setGviId(Long gviId) {
        this.gviId = gviId;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public GVIBase(String kod, String name, Integer value, String unit, String type, String view,
                   Integer adres, Integer write, Integer min, Integer max, Integer def, Integer koef, Integer size , String description) {
        this.kod = kod;
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.type = type;
        this.view = view;
        this.adres = adres;
        this.write = write;
        this.min = min;
        this.max = max;
        this.def = def;
        this.koef = koef;
        this.size = size;
        this.description = description;
    }

    public GVIBase() {
    }

    @Override
    public String toString() {
        return kod + " : " + name + " : " + value + " : " + unit + " : " + type+ " : " + view + " : " + adres
                + " : " + write + " : " + min + " : " + max + " : " + min + " : " + def + " : " + koef + " : " + size + " : " + description + " : " + model;

    }

}
