package com.gestionremodelacion.gestion.export;

import java.util.List;

public interface Exportable {

    List<String> getExportHeaders();

    List<List<String>> getExportData();
    //List<List<Object>> getExportData(); 

}
