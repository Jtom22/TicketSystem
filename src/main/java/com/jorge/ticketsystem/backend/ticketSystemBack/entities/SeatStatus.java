package com.jorge.ticketsystem.backend.ticketSystemBack.entities;

public enum SeatStatus {

    //   El asiento está libre y disponible para que cualquier usuario lo seleccione.
    //  
    DISPONIBLE,

    // 
    //  El asiento está en proceso de compra (un usuario lo tiene en su carrito). 
    //  Este estado evita que otros usuarios lo seleccionen mientras se procesa el pago,
    //  pero puede volver a DISPONIBLE si el pago expira o falla.
    // 
    RESERVADO_TEMPORAL,

    
    //  El asiento ya ha sido pagado y emitido con éxito. 
    //  Nunca puede volver a estar disponible a menos que se cancele la compra.
    
    OCUPADO
}
