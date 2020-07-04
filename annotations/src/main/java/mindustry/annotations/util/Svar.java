package mindustry.annotations.util;

import com.sun.source.tree.VariableTree;
import mindustry.annotations.BaseProcessor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class Svar extends Selement<VariableElement>{

    public Svar(VariableElement e){
        super(e);
    }

    public boolean is(Modifier mod){
        return e.getModifiers().contains(mod);
    }

    public VariableTree tree(){
        return (VariableTree)BaseProcessor.trees.getTree(e);
    }
}
