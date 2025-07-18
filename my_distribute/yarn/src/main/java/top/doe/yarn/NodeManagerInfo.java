package top.doe.yarn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeManagerInfo implements Serializable {

    private  String host;
    private  int port;

}
