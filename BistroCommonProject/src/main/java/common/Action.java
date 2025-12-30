package common; 

public enum Action {
    // Reservation Actions
    GET_RESERVATION,
    UPDATE_RESERVATION,
    CANCEL_RESERVATION,
    RESERVATION_NOT_FOUND,
    SEND_VERIFICATION_CODE,
    
    // Guest Actions
    GET_GUEST,
    
    // Connection Actions
    DISCONNECT,
    
    //Member Actions
    GET_MEMBER,
    UPDATE_MEMBER,
    DELETE_MEMBER,
    MEMBER_IDENTIFICATION,
    MEMBER_NOT_FOUND,
    
    //Visit Actions
    GET_VISIT,
    START_VISIT,
    END_VISIT,
    CANCEL_VISIT,
    
    //BILL ACTIONS
    GET_BILL,
    UPDATE_BILL
}
