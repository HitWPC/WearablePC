package cn.hitftcl.wearablepc.Model;

import org.litepal.crud.DataSupport;

import java.util.Date;

public class EnvironmentTable extends DataSupport{
    private int id;
    private double temperature;
    private double pressure;
    private double humidity;
    private double SO2;
    private double NO;
    private double voltage;
    private Date date;
    private String IP;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public EnvironmentTable() {
    }

    public Date getDate() {
        return date;
    }

    public EnvironmentTable(double temperature, double pressure, double humidity, double SO2, double NO, double voltage, Date date) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.SO2 = SO2;
        this.NO = NO;
        this.voltage = voltage;
        this.date = date;
    }

    public EnvironmentTable(double temperature, double pressure, double humidity, double SO2, double NO, double voltage, Date date, String IP) {

        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.SO2 = SO2;
        this.NO = NO;
        this.voltage = voltage;
        this.date = date;
        this.IP = IP;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public EnvironmentTable(double temperature, double pressure, double humidity, double SO2, double NO, double voltage, String IP) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.SO2 = SO2;
        this.NO = NO;
        this.voltage = voltage;
        this.IP = IP;
        this.date = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getSO2() {
        return SO2;
    }

    public void setSO2(double SO2) {
        this.SO2 = SO2;
    }

    public double getNO() {
        return NO;
    }

    public void setNO(double NO) {
        this.NO = NO;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
}
