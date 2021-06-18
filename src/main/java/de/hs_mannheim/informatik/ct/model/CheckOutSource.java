package de.hs_mannheim.informatik.ct.model;

public enum CheckOutSource {
    NotCheckedOut, UserCheckout, AutomaticCheckout, RoomReset;

    public static CheckOutSource getDefault() {
            return CheckOutSource.UserCheckout;
        }
}
