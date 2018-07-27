package com.soriole.kademlia.core.store;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Date;

/**
 * Class to put a DataType along with the creation Date,expiration Date
 * @param <Type>
 */
public class TimeStampedData<Type> {
    private Type data;
    private long insertionTime;
    private long expirationTime;

    public TimeStampedData(@NotNull Type data, long expirationTime){
        this(data,expirationTime,new Date().getTime());
    }
    public TimeStampedData(@NotNull Type data, long expirationTime,long insertionTime){
        this.data = data;
        this.expirationTime=expirationTime;
        this.insertionTime=insertionTime;
    }
    // keep it package private
    boolean set(Type t){
        Type oldValue= data;
        this.data =t;
        this.refresh();
        return oldValue.equals(t);
    }
    // keep it package private
    public Type getData(){
        return data;
    }

    public long getExpirationTime(){
        return expirationTime;
    }
    public long getInsertionTime(){
        return insertionTime;
    }
    public boolean refresh(){
        this.insertionTime=new Date().getTime();
        return true;
    }
    public void updateExpirationTime(long newTime){
        this.expirationTime=newTime;
    }

    public static Comparator<TimeStampedData> getExpirationComparator(){
        return (t0, t1) -> Long.compare(t0.expirationTime,t0.expirationTime);
    }
    public static Comparator<TimeStampedData> getInsertionComparator() {
        return (t0, t1) -> Long.compare(t0.expirationTime, t0.expirationTime);
    }
    @Override
    public boolean equals(Object o){
        if(o==null){
            return data ==null;
        }
        if (o instanceof TimeStampedData){
            if(data !=null) {
                return ((TimeStampedData) o).data.equals(data);
            }
        }
        else if(data !=null)
        {
            return data.equals(o);
        }
        return false;
    }
    @Override
    public int hashCode(){
        return this.getData().hashCode();
    }

}
