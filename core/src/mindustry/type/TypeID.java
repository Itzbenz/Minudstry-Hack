package mindustry.type;

import arc.func.Prov;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.entities.traits.TypeTrait;

public class TypeID extends MappableContent{
    public final Prov<? extends TypeTrait> constructor;

    public TypeID(String name, Prov<? extends TypeTrait> constructor){
        super(name);
        this.constructor = constructor;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.typeid;
    }
}
