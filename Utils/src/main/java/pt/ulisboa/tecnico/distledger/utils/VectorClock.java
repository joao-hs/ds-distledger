package pt.ulisboa.tecnico.distledger.utils;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.ArrayList;
import java.util.List;

public class VectorClock {

    private final List<Integer> timeStamps;

    public VectorClock(){
        timeStamps = new ArrayList<>();
        timeStamps.add(0);
        timeStamps.add(0);
        timeStamps.add(0);
    }

    public VectorClock(List<Integer> ts){
        timeStamps = new ArrayList<>(ts);
    }

    public Integer getTS(Integer i){
        return timeStamps.get(i);
    }

    public void setTS(Integer i, Integer value){
        timeStamps.set(i, value);
    }

    //Greater or equal
    public boolean GE(VectorClock vc){
        for(int i = 0; i < timeStamps.size(); i++){
            if (vc.getTS(i)>timeStamps.get(i))
                return false;
        }
        return true;
    }

    public DistLedgerCommonDefinitions.VectorClock proto(){
        return DistLedgerCommonDefinitions.VectorClock.newBuilder().addAllTs(this.timeStamps).build();
    }

    public void merge(List<Integer> newTS){
        for(int i = 0; i < timeStamps.size(); i++){
            if (newTS.get(i)>timeStamps.get(i))
                timeStamps.set(i,newTS.get(i));
        }
    }

    public void merge(VectorClock other) {
        for (int i = 0; i < timeStamps.size(); i++) {
            if (other.getTS(i) > timeStamps.get(i))
                timeStamps.set(i, other.getTS(i));
        }
    }

    @Override
    public String toString() {
        return "VectorClock{" +
                "timeStamps=" + timeStamps.toString() +
                '}';
    }
}


