package com.lynkteam.tapmanager.DB;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by robertov on 30/07/2015.
 */
public class Order
{
    public int orderId;
    public String shipAddress;
    public String billAddress;
    public String requestedTime;
    public String agreedTime;
    public int shippingPrice;
    public String orderStateCode;
    public String orderState;
    public String telephone;
    public int total;
    public String orderNumber;
    public String notes;
    public String shipBell;
    public String shipFloor;
    public String shipStreet;
    public String shipCity;
    public String shipTelephone;
    public String shipNameSurname;
    public boolean isPayed;
    public boolean isPreorder;
    public String lastEdit;
    public int shipVatRepart;
    public String printerStatus;
    public boolean isNew;      //ordine nuovo
    public int orderSource;
}
