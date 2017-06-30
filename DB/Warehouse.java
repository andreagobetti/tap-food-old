package com.lynkteam.tapmanager.DB;

/**
 * Created by robertov on 02/09/15.
 */
public class Warehouse {

    //popolare gli oggetti con criterio

    //automaticamente  pescare da warehouse element se necessairo

    public int warehouseId;
    public int warehouseElementId;
    public int qty;
    public int price;
    public String name;
    public boolean isSetUp;
    public boolean isAvailable; // = sia disponibile come ingrendiente che come prodotto
    public String lastEdit;

}
