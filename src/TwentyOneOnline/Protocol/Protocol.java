package TwentyOneOnline.Protocol;

public enum Protocol {
    C1_LOGIN,
    C2_SET_NICK,
    C3_SYNCHRINIZE_NICKS,
    C4_SET_INITIAL_ROLE,
    C5_COINS_UPDATE,
    C6_POOL_UPDATE,
    C7_BID_UPDATE,
    C8_DEAL_ONE_CARD_TO_EACH_PLAYER,
    C9_GET_DEALED_CARDS,
    C10_INFORM_OPPONENT_ABOUT_SET_BID,
    C11_SET_BID,
    C12_INFORM_OPPONENT_ABOUT_ACCEPTED_BID,
    C13_BID_ACCEPTED,
    C14_CHECK_POINTS,
    C15_TAKE_AN_CARD,
    C16_SHOW_ROLLED_CARD,
    C17_SHOW_GREYED_CARD,
    C18_LOSS,
    C19_POINT,
    C20_SHOW_ALL_CARDS,
    C21_LET_TO_TAKE_CARD_BANKER,
    C22_TAKE_CARD_BANKER,
    C23_WHOO_WINS,
    C24_WIN,
    C25_LOST,
    C26_CARDS_AT_TABLE,
    C27_CHANGE_ROLE,
    C28_ADD_MONEY_OPPONENT,
    C29_STAYING_IN_GAME,
    C30_TWO_DEALS,
    C31_END_OF_GAME,
    C32_LOGOUT
}
