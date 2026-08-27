import java.util.HashMap;
import java.util.Map;
class Sol<k,v>{
  private final Map<k,v> map=new HashMap<>();
  public void put(k key, v value){
    map.put(key,value);
  }
  public v get(k key){
    return map.get(key);
  }
  public void delete(k key){
    map.remove(key);
  }
  public void clear(){
    map.clear();
  }
}
public class Main(){
  public static void main(String[] args){
    Sol<String, String> cache=new Sol<>();
    cache.put("user1", "Alice");
    cache.put("user2", "Bob");
    cache.get("user1");
    cache.get("user3");
    cache.delete("user1");
  }
}
    
