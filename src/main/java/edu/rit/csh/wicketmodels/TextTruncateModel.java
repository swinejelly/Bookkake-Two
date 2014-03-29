package edu.rit.csh.wicketmodels;

import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Created by scott on 3/28/14.
 */
public class TextTruncateModel<T extends Serializable> implements IChainingModel {
    private int numSentences;
    private IModel<T> t;

    public TextTruncateModel(IModel<T> model, int numSentences){
        setChainedModel(model);
        this.numSentences = Math.max(numSentences, 0);
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
        if (t != null){
            T elem = t.getObject();
            String s = t.getObject().toString();
            System.out.println(s);
            int endIndex = nthOccurrence(s, '.', numSentences);
            if (endIndex == -1) endIndex = s.length();
            s = s.substring(0, endIndex);
            return s;
        }else{
            return null;
        }
    }

    /**
     * Return the index of the nth most instance of c,
     * returning the n-1th most index if not found, and so on.
     * If c not found, return -1.
     */
    private static int nthOccurrence(String str, char c, int n) {
        int pos = -1;
        while (n-- >= 0) {
            int next = str.indexOf(c, pos + 1);
            if (next == -1)
                return pos+1;
            else
                pos = next;
        }
        return pos+1;
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
