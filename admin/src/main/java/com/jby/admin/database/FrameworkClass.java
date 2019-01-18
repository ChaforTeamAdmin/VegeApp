package com.jby.admin.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class FrameworkClass {
    private Context context;
    private SQLiteOpenHelper sqLiteOpenHelper;
    private String table;
    private static final String TAG = "FrameworkClass";
    private ResultCallBack resultCallBack;

    public FrameworkClass(Context context, SQLiteOpenHelper sqLiteOpenHelper, String table) {
        this.context = context;
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        this.table = table;
    }

    public FrameworkClass(Context context, ResultCallBack resultCallBack, SQLiteOpenHelper sqLiteOpenHelper, String table) {
        this.context = context;
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        this.table = table;
        this.resultCallBack = resultCallBack;
    }

    private ContentValues getContentValues(String[] storeColumn, String[] storeValue){
        ContentValues contentValues = new ContentValues();
        if(storeColumn.length == storeValue.length)
            for(int i = 0; i < storeColumn.length; i++){
                contentValues.put(storeColumn[i], storeValue[i]);
            }
        return contentValues;
    }

    private String[] getArrayFromString(String input){
        if(!input.equals("")){
            String[] stringArray = input.split(",");
            for(int i = 0; i < stringArray.length; i++){
                stringArray[i] = stringArray[i].trim();
            }
            return  stringArray;
        }
        else return null;
    }

    /*-----------------------------------------------------------create----------------------------------------------------------------------*/
    //store chat detail into chat room

    public class create{
        SQLiteDatabase db;
        String storeColumn, storeValue;
        String[] storeColumnArray, storeValueArray;

        public create(String storeColumn, String storeValue){
            this.db = sqLiteOpenHelper.getWritableDatabase();
            this.storeColumn = storeColumn;
            this.storeValue = storeValue;
        }

        public void perform() {
            ContentValues contentValues;
            if(getStoreColumnArray(storeColumn).length != getStoreValueArray(storeValue).length){
                Toast.makeText(context, "Number of parameter not matched!", Toast.LENGTH_SHORT).show();
                return;
            }
            else
                 contentValues = getContentValues(getStoreColumnArray(storeColumn), getStoreValueArray(storeValue));
            //perform
            try {
                if (contentValues != null)  db.insert(table, null, contentValues);
                //read result
                if(resultCallBack!= null) resultCallBack.createResult("Success");
            }catch (SQLException e){
                //read result
                if(resultCallBack!= null) resultCallBack.createResult("Fail");
                Toast.makeText(context, "Invalid Parameter", Toast.LENGTH_SHORT).show();
            }
        }

        private String[] getStoreColumnArray(String storeColumn){
            storeColumnArray = storeColumn.split(",");
            for(int i = 0; i < storeColumnArray.length; i++){
                storeColumnArray[i] = storeColumnArray[i].trim();
            }
            return  storeColumnArray;
        }

        private String[] getStoreValueArray(String storeValue){
            storeValueArray = storeValue.split(",");
            for(int i = 0; i < storeValueArray.length; i++){
                storeValueArray[i] = storeValueArray[i].trim();
            }
            return  storeValueArray;
        }
    }

    /*---------------------------------------------------------------read-----------------------------------------------------------------*/
    public class Read {
        private String selectColumn = null;
        private SQLiteDatabase db;
        private String query = "";
        private String[] selectColumnArray;

        public Read(String selectColumn){
            this.selectColumn = selectColumn;
            db = sqLiteOpenHelper.getReadableDatabase();
            select();
        }

        //select query
        private void select(){
            query = "SELECT  "+ selectColumnValue(selectColumn) + "FROM " + table;
        }

        //where query
        public Read where(String whereCondition){
            if(whereCondition != null) query += " WHERE " + whereCondition;
            return this;
        }

        //ascending order
        public Read orderByAsc(String column){
            if(column != null) query += " ORDER BY " + column + " ASC";
            return this;
        }

        //descending order
        public Read orderByDesc(String column){
            if(column != null) query += " ORDER BY " + column + " DESC";
            return this;
        }

        //count row
        public int count(){
            int count = 0;
            try{
                //perform read action
                Cursor crs = db.rawQuery(query, null);
                count = crs.getCount();
                db.close();
                crs.close();

            }catch (SQLException e){
                Toast.makeText(context, "Invalid Parameter", Toast.LENGTH_SHORT).show();
            }
            return count;
        }

        //perform select action
        public void perform(){
            StringBuilder result = new StringBuilder();
            try{
                //perform read action
                Cursor crs = db.rawQuery(query, null);
                //bind result
                while (crs.moveToNext()) {
                    //first
                    if(crs.getPosition() == 0)
                        //only one item
                        if(crs.getCount() == 1) result.append("{ result:[").append(bindResult(crs, selectColumn)).append("]}");
                        else result.append("{ result:[").append(bindResult(crs, selectColumn));
                        //last
                    else if(crs.getPosition() == crs.getCount() -1)
                        result.append(",").append(bindResult(crs, selectColumn)).append("]}");
                    else
                        result.append(",").append(bindResult(crs, selectColumn));
                }
                db.close();
                crs.close();

            }catch (SQLException e){
                Toast.makeText(context, "Invalid Parameter", Toast.LENGTH_SHORT).show();
            }
            if(resultCallBack!= null) resultCallBack.readResult(result.toString());
        }
        //bind fetch result
        private String bindResult(Cursor cursor, String selectColumn){
            StringBuilder bindResult = new StringBuilder("{");
            for(int i = 0; i < cursor.getColumnCount(); i ++){
                //if select all
                if(selectColumn.equals("*")){
                    if(i == cursor.getColumnCount()-1)
                        bindResult.append(cursor.getColumnName(i)).append(":\"").append(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)))).append("\"}");
                    else
                        bindResult.append(cursor.getColumnName(i)).append(":\"").append(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)))).append("\",");
                }
                //else
                else{
                    if(i == cursor.getColumnCount()-1){
                        bindResult.append(cursor.getColumnName(i)).append(":\"").append(cursor.getString(cursor.getColumnIndex(selectColumnArray[i]))).append("\"}");
                    }
                    else{
                        bindResult.append(cursor.getColumnName(i)).append(":\"").append(cursor.getString(cursor.getColumnIndex(selectColumnArray[i]))).append("\",");
                    }
                }
            }
            return bindResult.toString();
        }
        //set select field
        private String selectColumnValue(String selectColumn){
            StringBuilder selectColumns = new StringBuilder();
            //if select all then return *
            if(selectColumn.equals("*")){
                return "*";
            }
            //else have to go through every select columns
            else{
                try {
                    //convert SELECT column from string to array
                    selectColumnArray = getSelectColumnArray(selectColumn);
                    for (int i = 0; i < selectColumnArray.length; i++) {
                        //if i != last item
                        if (i != selectColumnArray.length - 1)
                            selectColumns.append(selectColumnArray[i]).append(" , ");
                            //if i == last item
                        else selectColumns.append(selectColumnArray[i]).append(" ");
                    }
                }catch (IndexOutOfBoundsException e){
                    Log.d("", "" + e);
                }
                return selectColumns.toString();
            }
        }

        private String[] getSelectColumnArray(String selectColumn){
            selectColumnArray = selectColumn.split(",");
            for(int i = 0; i < selectColumnArray.length; i++){
                selectColumnArray[i] = selectColumnArray[i].trim();
            }
            return  selectColumnArray;
        }
    }

    /*---------------------------------------------------------------edit------------------------------------------------------------------*/
    public class Update{
        SQLiteDatabase db;
        String updateColumn, updateValue, whereValue = "";
        String[] updateColumnArray, updateValueArray;

        String where = null;

        public Update(String updateColumn, String updateValue){
            this.db = sqLiteOpenHelper.getWritableDatabase();
            this.updateColumn = updateColumn;
            this.updateValue = updateValue;
        }

        public Update where(String whereCondition, String whereValue){
            where = whereCondition;
            this.whereValue = whereValue;
            return this;
        }

        public void perform() {
            ContentValues contentValues;
            updateColumnArray = getArrayFromString(updateColumn);
            updateValueArray = getArrayFromString(updateValue);
            //length not match then stop
            assert updateValueArray != null;
            if(updateColumnArray.length != updateValueArray.length){
                Toast.makeText(context, "Number of parameter not matched!", Toast.LENGTH_SHORT).show();
                return;
            }
            else contentValues = getContentValues(updateColumnArray, updateValueArray);
            try {
                if (contentValues != null) db.update(table, contentValues, where, getArrayFromString(whereValue));
                if(resultCallBack!= null) resultCallBack.updateResult("Success");
            }catch (SQLException e){
                if(resultCallBack!= null) resultCallBack.updateResult("Fail");
                Log.d("haha", "haha: " + e);
                Toast.makeText(context, "Invalid Parameter", Toast.LENGTH_SHORT).show();
            }
            catch (IllegalArgumentException e){
                Toast.makeText(context, "Format Not Matched!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class Delete{
        SQLiteDatabase db;
        String whereValue = "";
        String where = "";

        public Delete(){
            this.db = sqLiteOpenHelper.getWritableDatabase();
        }

        public Delete where(String whereCondition, String whereValue){
            where = whereCondition;
            this.whereValue = whereValue;
            return this;
        }

        public long perform() {
            long result = -1;
            try {
                result = db.delete(table, where, getArrayFromString(whereValue));

                //delete result
                if(resultCallBack!= null) resultCallBack.deleteResult("Success");
            }catch (SQLException e){
                //delete result
                if(resultCallBack!= null) resultCallBack.deleteResult("Fail");
                Toast.makeText(context, "Invalid Parameter", Toast.LENGTH_SHORT).show();
            }
            return result;
        }

    }
}
