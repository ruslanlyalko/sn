package com.ruslanlyalko.sn.data.models;

/**
 * Created by Ruslan Lyalko
 * on 11.01.2018.
 */

public class Result {

    private int income;
    private int salary;
    private int expenses;
    private int profit;
    private String year;
    private String month;

    public Result() {
        //required
    }

    public Result(final int income, final int salary, final int expenses, final int profit, final String year, final String month) {
        this.income = income;
        this.salary = salary;
        this.expenses = expenses;
        this.profit = profit;
        this.year = year;
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(final String month) {
        this.month = month;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(final int income) {
        this.income = income;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(final int salary) {
        this.salary = salary;
    }

    public int getExpenses() {
        return expenses;
    }

    public void setExpenses(final int expenses) {
        this.expenses = expenses;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(final int profit) {
        this.profit = profit;
    }
}
