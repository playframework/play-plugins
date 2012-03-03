package service;

public abstract class MyService {

  protected Something s;

  public MyService(Something s) {
    this.s = s;
  }

  abstract public String demonstrate();
}