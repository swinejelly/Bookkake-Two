package edu.rit.csh.wicketmodels;

import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Created by scott on 3/28/14.
 */
public class DefaultModel<T extends Serializable> implements IChainingModel {
    private T defaultObject;
    private IModel<T> t;

    public DefaultModel(IModel<T> model, T defaultObject){
        setChainedModel(model);
        this.defaultObject = defaultObject;
    }

    @Override
    public void setChainedModel(IModel iModel) {
        t = iModel;
    }

    @Override
    public IModel<?> getChainedModel() {
        return t;
    }

    @Override
    public Object getObject() {
        String s = t.getObject().toString();
        if (s == null || s.toString().trim().isEmpty()){
            return defaultObject;
        }else{
            return t.getObject();
        }
    }

    @Override
    public void setObject(Object o) {
        if (o instanceof IModel){
            t = (IModel)o;
        }else{
            T elem = (T) o;
            t = Model.of(elem);
        }
    }

    @Override
    public void detach() {
        if (t != null){
            t.detach();
        }
    }
}
