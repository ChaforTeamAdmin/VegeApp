package com.jby.admin.object;

public class StockObject {
    private String date, totalIn, totalOut;
    private String target_id, target, in_quantity, out_quantity;



    public StockObject(String date, String totalIn, String totalOut, String in_quantity, String out_quantity) {
        this.date = date;
        this.totalIn = totalIn;
        this.totalOut = totalOut;
        this.in_quantity = in_quantity;
        this.out_quantity = out_quantity;
    }

    public StockObject(String target_id, String target,  String totalIn, String totalOut) {
        this.target_id = target_id;
        this.target = target;
        this.totalIn = totalIn;
        this.totalOut = totalOut;
    }

    public String getDate() {
        return date;
    }

    public String getTotalIn() {
        return totalIn;
    }

    public String getTotalOut() {
        return totalOut;
    }

    public String getTarget_id() {
        return target_id;
    }

    public String getTarget() {
        return target;
    }

    public String getIn_quantity() {
        return in_quantity;
    }

    public String getOut_quantity() {
        return out_quantity;
    }

    @Override
    public String toString() {
        return date + "  " + calculateTotalWeight() + " KG";
    }

    public String calculateTotalWeight() {
        try {
            return String.valueOf((Double.valueOf(totalIn)) - (Double.valueOf(totalOut)));
        } catch (NullPointerException e) {
            return "0";
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    public String calculateTotalQuantity(){
        try{
            return String.valueOf(Double.valueOf(in_quantity) - Double.valueOf(out_quantity));
        }catch (NullPointerException e){
            return "0";

        }catch (NumberFormatException e){
            return "0";
        }
    }
}
