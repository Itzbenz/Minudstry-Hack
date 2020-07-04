package mindustry.annotations.util;

import arc.struct.Array;
import com.squareup.javapoet.TypeName;
import com.sun.source.tree.MethodTree;
import mindustry.annotations.BaseProcessor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

public class Smethod extends Selement<ExecutableElement>{

    public Smethod(ExecutableElement executableElement){
        super(executableElement);
    }

    public boolean is(Modifier mod){
        return e.getModifiers().contains(mod);
    }

    public Array<TypeMirror> thrown(){
        return Array.with(e.getThrownTypes()).as(TypeMirror.class);
    }

    public Array<TypeName> thrownt(){
        return Array.with(e.getThrownTypes()).map(TypeName::get);
    }

    public Array<TypeParameterElement> typeVariables(){
        return Array.with(e.getTypeParameters()).as(TypeParameterElement.class);
    }

    public Array<Svar> params(){
        return Array.with(e.getParameters()).map(Svar::new);
    }

    public TypeMirror ret(){
        return e.getReturnType();
    }

    public TypeName retn(){
        return TypeName.get(ret());
    }

    public MethodTree tree(){
        return BaseProcessor.trees.getTree(e);
    }
}
