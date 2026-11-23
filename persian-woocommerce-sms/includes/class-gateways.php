<?php if ( ! defined( 'ABSPATH' ) ) {
    exit;
}

class WoocommerceIR_SMS_Gateways {

    public $mobile = array();
    public $message = '';
    public $senderNumber = '';

    private $username = '';
    private $password = '';

    private static $_instance;

    public static function init() {
        if ( ! self::$_instance ) {
            self::$_instance = new self();
        }

        return self::$_instance;
    }

    public function __construct() {
        $this->username     = PWooSMS()->Options( 'sms_gateway_username' );
        $this->password     = PWooSMS()->Options( 'sms_gateway_password' );
        $this->senderNumber = PWooSMS()->Options( 'sms_gateway_sender' );
    }

    public static function get_sms_gateway() {

        $gateway = array(

            'raygansms'     => 'RayganSms.com',

        );

        return apply_filters( 'pwoosms_sms_gateways', $gateway );
    }



    public function raygansms()
    {
        require_once __DIR__ . "/class-raygansms.php";
        $raygansms = new pwoo_sms_raygansms();
        return $raygansms->SendSMS([
            "mobile" => $this->mobile,
            "message" => $this->message,
            "senderNumber" => $this->senderNumber,
        ]);
    }

}