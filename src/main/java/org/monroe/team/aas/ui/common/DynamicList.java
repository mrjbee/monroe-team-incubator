package org.monroe.team.aas.ui.common;

import java.util.LinkedList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/29/13 Time: 10:13 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class DynamicList <Type> {

    private List<Type> mInnerList = new LinkedList<Type>();

    public abstract Type createElement();

    public Type get(int ind){
        if (mInnerList.size() <= ind){
            //Expand list with new items
            int overDelta  = ind - (mInnerList.size()-1);
            for (int i =0 ;i<overDelta;i++){
                mInnerList.add(mInnerList.size(), createElement());
            }
        }
        return mInnerList.get(ind);
    }

    public void add(int ind, Type item){
        if(ind != 0){
            //create empty items if not exists
            get(ind-1);
        }
        mInnerList.add(ind, item);
    }

    public Type delete(int ind){
        if (has(ind)){
            return mInnerList.remove(ind);
        }
        return null;
    }

    public boolean has(int ind){
        return mInnerList.size() > ind;
    }

    public int size(){
        return mInnerList.size();
    }
}
