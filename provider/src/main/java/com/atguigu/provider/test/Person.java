package com.atguigu.provider.test;

import java.util.Objects;

public class Person {
    private String name;
    private String age;
    private Integer salary;
    public void test1(){
        Person person = new Person();
        person.equals(person);
        person.hashCode();
    }
}
