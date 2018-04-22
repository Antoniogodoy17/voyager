package voyager.voyager;

import java.text.DateFormat;

public class User {
    String name;
    String email;
    String password;
    String birth_date;
    String nationality;
    String state;
    String city;

    public User(String name,String email,String password,String birth_date,String nationality,String state,String city){
        this.name = name;
        this.email = email;
        this.password = password;
        this.birth_date = birth_date;
        this.nationality = nationality;
        this.state = state;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public String getNationality() {
        return nationality;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCity(String city) {
        this.city = city;
    }
}