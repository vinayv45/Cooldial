package com.droideve.apps.nearbystores.appconfig;

import com.droideve.apps.nearbystores.classes.Category;

import java.util.List;

public class Constances {


    public static int DISTANCE_CONST = 1024;
    //Change the url depending on the name of your web hosting
    // public static String BASE_URL = "https://droideve.com/smartgeostore";
    public static String BASE_URL = AppConfig.BASE_URL;
    public static String BASE_URL_API = AppConfig.BASE_URL;
    public static String TERMS_OF_USE_URL = AppConfig.BASE_URL;
    public static String PRIVACY_POLICY_URL = AppConfig.BASE_URL;
    public static String FORGET_PASSWORD = AppConfig.BASE_URL + "/fpassword";


    public static class ModulesConfig {
        public final static String SERVICE_PAYMENT_MODULE = "booking_payment";
        public final static String STORE_MODULE = "store";
        public final static String SERVICE_MODULE = "service";
        public final static String OFFER_MODULE = "offer";
        public final static String EVENT_MODULE = "event";
        public final static String MESSENGER_MODULE = "messenger";
        public final static String SLIDER_MODULE = "nsbanner";
        public final static String BOOKING_MODULE = "booking";
    }

    public static class OrderByFilter {
        public static final String RECENT = "recent";
        public static final String NEARBY = "nearby";
        public static final String UPCOMING = "upcoming";
        public static final String TOP_RATED = "top_rated";
        public static final String NEARBY_TOP_RATED = "nearby_top_rated";
    }


    //WARNING :  DO NOT EDIT THIS
    public static class API {

        public static String API_VERSION = "1.0";

        //setting API's
        public static String API_APP_INIT = BASE_URL_API + "/" + API_VERSION + "/setting/app_initialization";
        public static String API_GET_LANGUAGES = BASE_URL_API + "/" + API_VERSION + "/nstranslator/getLanguages";
        //Modules
        public static String API_AVAILABLE_MODULES = BASE_URL_API + "/" + API_VERSION + "/modules_manager/availableModules";
        public static String API_APP_CONFIG = BASE_URL_API + "/" + API_VERSION + "/setting/getAppConfig";


        //store API's
        public static String API_USER_GET_STORES = BASE_URL_API + "/" + API_VERSION + "/store/getStores";
        public static String API_USER_GET_REVIEWS = BASE_URL_API + "/" + API_VERSION + "/store/getComments";
        public static String API_USER_UPDATE_STORE = BASE_URL_API + "/" + API_VERSION + "/webservice/updateStore";
        public static String API_RATING_STORE = BASE_URL_API + "/" + API_VERSION + "/store/rate";
        public static String API_SAVE_STORE = BASE_URL_API + "/" + API_VERSION + "/store/saveStore";
        public static String API_REMOVE_STORE = BASE_URL_API + "/" + API_VERSION + "/store/removeStore";
        //event API's
        public static String API_USER_GET_EVENTS = BASE_URL_API + "/" + API_VERSION + "/event/getEvents";
        //category API's
        public static String API_USER_GET_CATEGORY = BASE_URL_API + "/" + API_VERSION + "/category/getCategories";
        //uploader API's
        public static String API_USER_UPLOAD64 = BASE_URL_API + "/" + API_VERSION + "/uploader/uploadImage64";
        //user API's
        public static String API_USER_LOGIN = BASE_URL_API + "/" + API_VERSION + "/user/signIn";
        public static String API_USER_SIGNUP = BASE_URL_API + "/" + API_VERSION + "/user/signUp";
        public static String API_USER_CHECK_CONNECTION = BASE_URL_API + "/" + API_VERSION + "/user/checkUserConnection";
        public static String API_BLOCK_USER = BASE_URL_API + "/" + API_VERSION + "/user/blockUser";
        public static String API_GET_USERS = BASE_URL_API + "/" + API_VERSION + "/user/getUsers";
        public static String API_UPDATE_ACCOUNT = BASE_URL_API + "/" + API_VERSION + "/user/updateAccount";
        public static String API_UPDATE_ACCOUNT_PASSWORD = BASE_URL_API + "/" + API_VERSION + "/user/updateAccountPassword";
        public static String API_USER_REGISTER_TOKEN = BASE_URL_API + "/" + API_VERSION + "/user/registerToken";
        public static String API_REFRESH_POSITION = BASE_URL_API + "/" + API_VERSION + "/user/refreshPosition";
        public static String API_DISABLE_ACCOUNT= BASE_URL_API + "/" + API_VERSION + "/user/disableAccount";
        public static String API_GENERATE_QRCODE_TOKEN = BASE_URL_API + "/user/generateUniqueQRCode";
        public static String API_FIND_USER_BY_TOKEN = BASE_URL_API + "/user/findUserByToken";
        public static String API_EXTERNAL_AUTH = BASE_URL_API+"/user/userAuth";


        //messenger API's
        public static String API_LOAD_MESSAGES = BASE_URL_API + "/" + API_VERSION + "/messenger/loadMessages";
        public static String API_LOAD_DISCUSSION = BASE_URL_API + "/" + API_VERSION + "/messenger/loadDiscussion";
        public static String API_INBOX_MARK_AS_SEEN = BASE_URL_API + "/" + API_VERSION + "/messenger/markMessagesAsSeen";
        public static String API_INBOX_MARK_AS_LOADED = BASE_URL_API + "/" + API_VERSION + "/messenger/markMessagesAsLoaded";
        public static String API_SEND_MESSAGE = BASE_URL_API + "/" + API_VERSION + "/messenger/sendMessage";
        //offer API's
        public static String API_GET_OFFERS = BASE_URL_API + "/" + API_VERSION + "/offer/getOffers";
        public static String API_OFFER_GET_COUPON_CODE = BASE_URL_API + "/" + API_VERSION + "/qrcoupon/getCouponCode";
        public static String API_GET_COUPONS = BASE_URL_API + "/" + API_VERSION + "/qrcoupon/getCoupons";
        public static String API_CHECK_COUPON = BASE_URL_API + "/" + API_VERSION + "/qrcoupon/checkCoupon";
        public static String API_REMOVE_COUPON = BASE_URL_API + "/" + API_VERSION + "/qrcoupon/remove";
        public static String API_UPDATE_COUPON_STATUS = BASE_URL_API + "/" + API_VERSION + "/qrcoupon/updateStatus";

        //campaign API's
        public static String API_MARK_VIEW = BASE_URL_API + "/" + API_VERSION + "/campaign/markView";
        public static String API_MARK_RECEIVE = BASE_URL_API + "/" + API_VERSION + "/campaign/markReceive";

        //gallery
        public static String API_GET_GALLERY = BASE_URL_API + "/" + API_VERSION + "/gallery/getGallery";

        //Slider
        public static String API_GET_SLIDERS = BASE_URL_API + "/" + API_VERSION + "/nsbanner/getBanners";

        //Notification API's
        public static String API_NOTIFICATIONS_GET = BASE_URL_API + "/" + API_VERSION + "/nshistoric/getNotifications";
        public static String API_NOTIFICATIONS_COUNT_GET = BASE_URL_API + "/" + API_VERSION + "/nshistoric/getCount";
        public static String API_NOTIFICATIONS_EDIT_STATUS = BASE_URL_API + "/" + API_VERSION + "/nshistoric/changeStatus";
        public static String API_NOTIFICATIONS_REMOVE = BASE_URL_API + "/" + API_VERSION + "/nshistoric/remove";
        public static String API_NOTIFICATIONS_AGREEMENT = BASE_URL_API + "/" + API_VERSION + "/campaign/notification_agreement";

        //Bookmark API's
        public static String API_BOOKMARK_STORE_SAVE = BASE_URL_API + "/" + API_VERSION + "/store/saveStore";
        public static String API_BOOKMARK_STORE_REMOVE = BASE_URL_API + "/" + API_VERSION + "/store/removeStore";
        public static String API_BOOKMARK_EVENT_SAVE = BASE_URL_API + "/" + API_VERSION + "/event/saveEventBK";
        public static String API_BOOKMARK_EVENT_REMOVE = BASE_URL_API + "/" + API_VERSION + "/event/removeEventBK";
        public static String API_BOOKMARK_OFFER_SAVE = BASE_URL_API + "/" + API_VERSION + "/offer/saveBookmarkOffer";
        public static String API_BOOKMARK_OFFER_REMOVE = BASE_URL_API + "/" + API_VERSION + "/offer/removeBookmarkOffer";
        public static String API_BOOKMARKS_GET = BASE_URL_API + "/" + API_VERSION + "/bookmark/getBookmarks";
        public static String API_BOOKMARKS_REMOVE = BASE_URL_API + "/" + API_VERSION + "/bookmark/remove";


        //Orders
        public static String API_BOOKING_GET = BASE_URL_API + "/" + API_VERSION + "/booking/getBookings";
        public static String API_BOOKING_CREATE = BASE_URL_API + "/" + API_VERSION + "/booking/createBooking";
        public static String API_UPDATE_BOOKING_BUSINESS = BASE_URL_API + "/" + API_VERSION + "/booking/updateBooking";
        public static String API_UPDATE_BOOKING_CLIENT = BASE_URL_API + "/" + API_VERSION + "/booking/updateBookingClient";
        public static String API_CHECK_BOOKING = BASE_URL_API + "/" + API_VERSION + "/booking/checkBooking";




        //payment
        public static String API_PAYMENT_GATEWAY = BASE_URL_API + "/" + API_VERSION + "/booking_payment/getPayments";
        public static String API_PAYMENT_LINK = BASE_URL_API + "/" + API_VERSION + "/booking_payment/get_payment_link";
        public static String API_PAYMENT_LINK_CALL = BASE_URL + "/booking_payment/link_call";


        //report issue
        public static String API_REPORT_ISSUE = BASE_URL_API + "/" + API_VERSION + "/setting/content_report";

    }


    public static class initConfig {

        //WARNING :  DO NOT EDIT THIS
        public static List<Category> ListCats;
        public static int Numboftabs;

        public static class fonts {
        }

        //WARNING :  DO NOT EDIT THIS
        public static class Tabs {

            public static final int HOME = 0;
            public static final int BOOKMAKRS = -1;
            public static final int MOST_RATED = -2;
            public static final int MOST_RECENT = -3;
            public static final int EVENTS = -4;
            public static final int CHAT = -5;
            public static final int NEARBY_OFFERS = -6;
        }

    }


}
