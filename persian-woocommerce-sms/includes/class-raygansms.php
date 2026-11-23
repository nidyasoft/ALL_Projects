<?php
if (!defined('ABSPATH')) {
    exit;
}
if (!function_exists('is_plugin_active')) {
    include_once(ABSPATH . '/wp-admin/includes/plugin.php');
}
if (!class_exists('WoocommerceIR_SMS_Gateways')) {
    require_once WP_PLUGIN_DIR . '/persian-woocommerce-sms/WoocommerceIR_SMS.php';
}
if (!class_exists('pwoo_sms_raygansms') && class_exists('WoocommerceIR_SMS_Gateways')) {
    class pwoo_sms_raygansms extends WoocommerceIR_SMS_Gateways
    {

        const PATH = "https://smspanel.trez.ir/%s.ashx";

        const PATH2 = "https://raygansms.com/api/smsAPI/%s";
        public $mobile = array();
        public $message = '';
        public $senderNumber = '';
        private $username = '';
        private $password = '';
        private $apiKey = '';
        private $domain = '';
        private static $_instance;

        public static function init()
        {
            if (!self::$_instance) {
                self::$_instance = new parent();
            }
            return self::$_instance;
        }

        public function raygansms()
        {
            exit("oooooo");
        }

        public function __construct()
        {
            ini_set("display_errors", 1);
            $this->username = PWooSMS()->Options('sms_gateway_username');
            $this->password = PWooSMS()->Options('sms_gateway_password');
            $this->senderNumber = PWooSMS()->Options('sms_gateway_sender');
            $this->apiKey = PWooSMS()->Options('sms_gateway_apikey');
            add_filter('pwoosms_main_settings', array($this, 'pwoosms_main_settings_raygansms'));
            add_filter('persianwoosms_sms_gateway', array($this, 'raygansms_gateway'), 0);
            add_action('pwoosms_settings_form_bottom_sms_main_settings', array($this, 'pwoosms_super_admin_settings_raygansms'));
            add_action('pwoosms_settings_form_bottom_sms_super_admin_settings', array($this, 'patternscript'));
            add_action('pwoosms_settings_form_bottom_sms_buyer_settings', array($this, 'patternscript'));
            add_action('pwoosms_settings_form_bottom_sms_product_admin_settings', array($this, 'patternscript'));
            add_action('pwoosms_settings_form_bottom_sms_notif_settings', array($this, 'patternscript'), 10, 1);
            add_action('wp_ajax_pwoo_sms_raygansms_CheckPattern', array($this, 'checkPattern'));
            add_action('wp_ajax_pwoo_sms_raygansms_test', array($this, 'pwoo_sms_raygansms_test'));
            $gateway_object = self::init();
            if (!method_exists($gateway_object, 'raygansms')) {
                self::pwoosms_gateway_hack();
            }
        }


        public function pwoosms_gateway_hack()
        {
            $gateway_object = self::init();
            if (!method_exists($gateway_object, 'raygansms')) {
                $original_file = file_get_contents(WP_PLUGIN_DIR . '/persian-woocommerce-sms/includes/class-gateways.php');
                if (strpos($original_file, 'raygansms') === false) {
                    $raygansms_function = '	public function raygansms( $datas ) {
	    if(!class_exists("pwoo_sms_raygansms")){
	        require_once WP_PLUGIN_DIR . "/persian-woocommerce-sms-raygansms/pwoo_sms_raygansms.php";
	    }
		if(class_exists("pwoo_sms_raygansms")){
		$raygansms = new pwoo_sms_raygansms;
		return $raygansms->SendSMS( $datas );
	}else{
		return "افزونه مکمل raygansms غیرفعال است. لطفا از صفحه افزونه ها فعال نمایید.";
	}
}
}';
                    $new_file = strrev(implode(strrev($raygansms_function), explode(strrev("}"), strrev($original_file), 2)));
                    file_put_contents(WP_PLUGIN_DIR . '/persian-woocommerce-sms/includes/class-gateways.php', $new_file);
                }
            }
        }

        public function pwoosms_main_settings_raygansms($settings)
        {
            $newordersettings = array();
            foreach ($settings as $setting) {
                if ($setting['name'] == 'sms_gateway_sender') {
                    $newordersettings[] = $setting;
                    $newordersettings[] = array('name' => 'sms_gateway_apikey', 'label' => '', 'type' => 'text', 'ltr' => 1, 'desc' => 'برای ارسال با پترن لطفا کلید دسترسی را وارد کنید', 'default' => '',);
                } else {
                    $newordersettings[] = $setting;
                }
            }
            return $newordersettings;
        }

        public function pwoosms_super_admin_settings_raygansms()
        {
            ?>
            <script type="text/javascript">
                jQuery(document).ready(function () {
                    jQuery('select#sms_main_settings\\[sms_gateway\\]').on('change', function () {
                        if (jQuery(this).val() == 'raygansms') {
                            jQuery('#sms_main_settings\\[sms_gateway_domain\\]').parents('tr').show();
                        } else {
                            jQuery('#sms_main_settings\\[sms_gateway_domain\\]').parents('tr').hide();
                        }
                    }).change();

                });
            </script>
        <?php }

        protected function getPath($path, $method)
        {
            return sprintf($path, $method);
        }

        public function raygansms_gateway($gateway)
        {
            $gateway['raygansms'] = 'raygansms.com';
            return $gateway;
        }


        public function debug($data = null)
        {
            $file = @file_get_contents('pwoo_debug.txt');
            if (is_array($data) || is_object($data)) $data = json_encode($data);
            @file_put_contents('pwoo_debug.txt', $file . "\n" . $data);
            return;
        }

        public function SendSMS($datas)
        {
            $response = false;
            $username = trim($this->username);
            $password = trim($this->password);
            $apikey = trim($this->apiKey);
            $from = trim($this->senderNumber);

            if ((empty($username) || empty($password)) && empty($apikey)) {
                return false;
            }
            $massage = trim(strip_tags($datas['message']));
            if (is_array($datas['mobile'])) $to = $datas['mobile']; else $to = explode(',', $datas['mobile']);
            $massage = str_replace('pcode:', 'patterncode:', strip_tags(trim($massage)));
            if (substr($massage, 0, 11) === "patterncode") {
                $massage = str_replace("\r\n", ";", $massage);
                $massage = str_replace("\n", ";", $massage);
                $splited = explode(';', $massage);
                $patterncodeArray = explode(':', $splited[0]);
                $patterncode = trim($patterncodeArray[1]);
                unset($splited[0]);
                $input_data = array();
                foreach ($splited as $parm) {
                    $splited_parm = explode(':', $parm, 2);
                    $input_data[$splited_parm[0]] = trim($splited_parm[1]);
                }

                foreach ($to as $toNum) {
                    if ($from == NULL || $from == "" || strlen($from) < 5) {
                        $url = $this->getPath(self::PATH, 'SendPatternCodeWithUrl');
                        $data = [
                            'AccessHash' => $apikey,
                            'PatternId' => $patterncode,
                            'Mobile' => $toNum,
                            'token1' => @$input_data["token1"],
                            'token2' => @$input_data["token2"],
                            'token3' => @$input_data["token3"],
                            'token4' => @$input_data["token4"],
                            'token5' => @$input_data["token5"],
                            'token6' => @$input_data["token6"],
                            'token7' => @$input_data["token7"],
                            'token8' => @$input_data["token8"],
                            'token9' => @$input_data["token9"],
                        ];
                        $result = $this->execute($url, $data, FALSE, TRUE);
                    } else {
                        $url = $this->getPath(self::PATH, 'SendPatternWithUrl');
                        $data = [
                            'AccessHash' => $apikey,
                            'PatternId' => $patterncode,
                            'PhoneNumber' => $from,
                            'RecNumber' => $toNum,
                            'token1' => @$input_data["token1"],
                            'token2' => @$input_data["token2"],
                            'token3' => @$input_data["token3"],
                            'token4' => @$input_data["token4"],
                            'token5' => @$input_data["token5"],
                            'token6' => @$input_data["token6"],
                            'token7' => @$input_data["token7"],
                            'token8' => @$input_data["token8"],
                            'token9' => @$input_data["token9"],
                            'Smsclass' => 1,
                        ];

                        $result = $this->execute($url, $data, FALSE, TRUE);
                    }
                }

                return $result;
            } else {
                foreach ($to as $toNum) {
                    if ($from == NULL || $from == "" || strlen($from) < 5) {
                        $params = [
                            'Mobile' => $toNum,
                            'Message' => $massage,
                        ];
                        $url = $this->getPath(self::PATH2, 'SendCode');
                        $result = $this->execute($url, $params, TRUE, FALSE);
                    } else {
                        $params = [
                            'PhoneNumber' => $from,
                            'Mobiles' => [$toNum],
                            'Message' => $massage,
                            'UserGroupID' => uniqid("digits"),
                            'SendDateInTimeStamp' => NULL,
                        ];
                        $url = $this->getPath(self::PATH2, 'SendMessage');
                        $result = $this->execute($url, $params, TRUE, FALSE);
                    }
                }
            }
            return $result;
        }


        public function pwoo_sms_raygansms_test()
        {
            $datas = array('message' => $_POST['body'], 'mobile' => explode(',', $_POST['phone']),);
            print $this->SendSMS($datas);
            die();
        }

        protected function execute($url, $data = NULL, $post = TRUE, $pattern = FALSE)
        {
            $fields_string = "";

            if (!is_null($data)) {
                $fields_string = http_build_query($data);
            }
            if (!$post) {
                if (!is_null($data)) {
                    $url = $url . "?" . $fields_string;
                }
            }
            $handle = curl_init();
            curl_setopt($handle, CURLOPT_URL, $url);
            curl_setopt($handle, CURLOPT_RETURNTRANSFER, TRUE);
            curl_setopt($handle, CURLOPT_FOLLOWLOCATION, TRUE);
            if ($post) {
                curl_setopt($handle, CURLOPT_POST, TRUE);
                curl_setopt($handle, CURLOPT_POSTFIELDS, json_encode($data));
                curl_setopt($handle, CURLOPT_HTTPHEADER, [
                    'Content-type: application/json',
                    'Authorization: Basic ' . base64_encode($this->username . ":" . $this->password),
                ]);
            }

            $response = curl_exec($handle);
            $code = curl_getinfo($handle, CURLINFO_HTTP_CODE);
            if ($code == "401") {
                exit("اطلاعات کاربری صحیح نیست");
            }
            $curl_errno = curl_errno($handle);

            $curl_error = curl_error($handle);
            if ($curl_errno) {
                throw new Exception($curl_error);
            }

            $response = json_decode($response);
            $result = @$response->Message ?? json_encode($response);
            if ($response >= 2000) return true;

            if (intval($response) >= 2000 || intval($response) == 0) {
                $is_success = 200;
                return $result;
            }
            return $result;

        }


        public function patternscript($form = null)
        {
            add_thickbox(); ?>
            <div id="pattern-teach" style="display:none;">
                <p>برای ایجاد الگو در پنل وارد سربرگ "سرویس برنامه نویسان-جدید" شوید. سپس در قسمت "ایجاد الگو" ، الگوی
                    مد نظر را ایجاد کنید. سپس در سربرگ "مدیریت الگوها" مشاهده ی الگوی مد نظر را کلیک کنید تا شناسه ی آن
                    را ببنید." )</p>
            </div>
            <div id="pattern-tool-wrapper" style="display:none;">
                <div id="pattern-tool">
                </div>
            </div>
            <script>
                (function ($) {
                    function activate_pwoosms_raygansms_pattern_tools() {
                        var textarea_index = 1;
                        $('textarea.regular-text').not('#sms_notif_settings\\[notif_options\\]').each(function () {
                            $(this).parents('tr').addClass('pwoo_raygansms_tools trindex-' + textarea_index).attr('id', 'trindex-' + textarea_index);
                            if ($(this).attr('id') == 'sms_super_admin_settings[admin_low_stock]' || $(this).attr('id') == 'sms_super_admin_settings[admin_out_stock]') $(this).parents('tr').addClass('outofstock');
                            textarea_index++;
                        });
                        $('tr.pwoo_raygansms_tools').find('th label').after('<div class="raygansms-tools"><a class="raygansms-patternlearn thickbox button button-secondary" href="#TB_inline?width=800&inlineId=pattern-teach">آموزش ثبت پترن</a><a class="raygansms-patterntool thickbox button button-secondary" href="#TB_inline?width=800&inlineId=pattern-tool-wrapper">ابزار تنظیم پترن</a><a class="raygansms-smstest button button-secondary">تست پیامک</a></div>');
                        $('.raygansms-patterntool').on('click', function () {
                            <?php if(isset($form['id']) && in_array($form['id'], array('sms_super_admin_settings', 'sms_buyer_settings', 'sms_product_admin_settings'))){?>
                            var targetvars = $('.pwoosms_settings_shortcodes').html().replace(/code/g, 'span');
                            if ($(this).parents('tr').hasClass('outofstock')) targetvars = '<div class="description" style="margin-top:4px;color:#616161;font-style:italic;font-size:13px;line-height:25px;"> شورت کد های قابل استفاده در متن پیامک های مرتبط با موجوی انبار :<br><span>{product_id}</span> : آیدی محصول، <span>{sku}</span> : شناسه محصول، <span>{product_title}</span> : عنوان محصول، <span>{stock}</span> : موجودی انبار</div>';
                            <?php
                            }elseif(isset($form['id']) && $form['id'] == 'sms_notif_settings'){?>
                            var targetvars = '<div class="description" style="margin-top:4px;color:#616161;font-style:italic;font-size:13px;line-height:25px;"> <span>{product_id}</span> : آیدی محصول، <span>{sku}</span> : شناسه محصول، <span>{product_title}</span> : عنوان محصول، <span>{regular_price}</span> قیمت اصلی، <span>{onsale_price}</span> : قیمت فروش فوق العاده<br><span>{onsale_from}</span> : تاریخ شروع فروش فوق العاده، <span>{onsale_to}</span> : تاریخ اتمام فروش فوق العاده، <span>{stock}</span> : موجودی انبار</div>';
                            <?php }else{?>
                            var targetvars = 'متغیری در صفحه یافت نشد. در صورتی که متغیرها را می دانید از آنها استفاده کنید.';
                            <?php     } ?>
                            var targetarea = $(this).parents('tr').find('textarea');
                            var patternToolText = 'پس از ثبت پترن در سامانه پیامک و تأیید آن، کد پترن را در کادر زیر وارد نمایید:<div id="onlinepattern"><label>کد پترن:</label> <input class="patterncodeinput" placeholder="کد پترن شما" type="text"><div><button class="onlinepcodechecker" type="button">بررسی پترن</button></div><div class="pcodecheckresult"></div><div class="variableshelp hidden">' + targetvars + '</div><div><button class="patterninsert hidden" type="button">ثبت پترن در کادر پیامک</button></div></div><br><div><b>راهنمایی:</b><p class="raygansmspatternstephelp"> متن پیامک خود را در سامانه پیامک > صفحه ارسال بر اساس پترن، تبدیل به پترن کنید. پس از مدتی که پترن شما تأیید شد، کد پترن را از همان صفحه ارسال بر اساس پترن کپی و در کادر فوق درج نمایید.</p></div>';


                            ///
                            $('div#pattern-tool').html(patternToolText);
                            var output = '<p>لطفاً پارامترهای پترن را با متغیرهای سایت تکمیل نمایید.</p>';
                            output += '<div><label>token1</label><input type="text"/></div>';
                            output += '<div><label>token2</label><input type="text"/></div>';
                            output += '<div><label>token3</label><input type="text"/></div>';
                            output += '<div><label>token4</label><input type="text"/></div>';
                            output += '<div><label>token5</label><input type="text"/></div>';
                            output += '<div><label>token6</label><input type="text"/></div>';
                            output += '<div><label>token7</label><input type="text"/></div>';
                            output += '<div><label>token8</label><input type="text"/></div>';
                            output += '<div><label>token9</label><input type="text"/></div>';
                            $('.pcodecheckresult').html(output);
                            $('.variableshelp,.patterninsert').removeClass('hidden');
                            $('.onlinepcodechecker').addClass('hidden');
                            $('.raygansmspatternstephelp').html('حالا پارامترهای پترن خود را در کادرهای ظاهر شده تکمیل نمایید. از شورتکدهای زیر می توانید استفاده نمایید.');
                            $('.patterninsert').click(function () {
                                pcode = $('#onlinepattern .patterncodeinput').val();
                                var patternoutput = 'pcode:' + pcode;

                                $('.pcodecheckresult div').each(function () {
                                    if (patternoutput !== '') patternoutput += "\n";
                                    patternoutput += $(this).find('label').text() + ':' + $(this).find('input').val();
                                })
                                targetarea.val(patternoutput).css('direction', 'ltr');
                                $('.tb-close-icon').click();
                            });


                        });
                        $('.raygansms-smstest').on('click', function () {
                            $('.send-test-sms-mobile').parent().remove();
                            var trid = $(this).parents('tr').attr('id');
                            $(this).after('<div><input class="send-test-sms-mobile" id="' + trid + '_test_mobile" type="text" placeholder="mobile" /><span class="send-test-sms-btn" id="' + trid + '_sendTestSms">ارسال</span><br><span class="result"></span></div>');
                            $('#' + trid + '_sendTestSms').on('click', function () {
                                var phone = $('input#' + trid + '_test_mobile').val();
                                var body = $('#' + trid).find('textarea').val();
                                var result = pwoo_sms_raygansms_test(phone, body, $(this).parent().find('span.result'));
                                if (result == 'phonenotfound') {
                                    $('input#' + trid + '_test_mobile').focus();
                                    return false;
                                }

                            });
                            return false;
                        });

                    }

                    function pwoo_sms_raygansms_test(phone, body, resultbox) {
                        if (phone === '') {
                            alert('لطفاً یک شماره همراه برای دریافت پیامک تست وارد کنید.');
                            return 'phonenotfound';
                        }
                        var data = {
                            action: 'pwoo_sms_raygansms_test',
                            phone: phone,
                            body: body,
                        };
                        $(".send-test-sms-btn").attr("disabled", 1)
                        $(".send-test-sms-mobile").attr("disabled", 1)
                        $.post(ajaxurl, data, function (resp) {
                            console.log(resp);
                            $(".send-test-sms-btn").removeAttr("disabled")
                            $(".send-test-sms-mobile").removeAttr("disabled")
                            if (resp == '1') {
                                resultbox.html('<span style="color:green">پیامک تست ارسال شد.</span>');
                            } else {
                                resultbox.html(`<span style="color:red">پیامک ارسال نشد. تنظیمات درگاه و اعتبار پنل پیامک و متن ارسالی را بررسی کنید : ${resp}.</span>`);
                            }
                        });
                        return false;
                    }

                    $(document).ready(function () {
                        activate_pwoosms_raygansms_pattern_tools();
                    });

                })(jQuery);
            </script>
            <style>
                .send-test-sms-mobile {
                    padding: 0px 2px !important;
                    direction: ltr;
                    width: 50%;
                    margin: 5px;
                }

                .send-test-sms-btn {
                    background: #1a7b2f;
                    border-color: #1a7b2f;
                    color: #fff;
                    text-decoration: none;
                    text-shadow: none;
                    cursor: pointer;
                    display: inline-block;
                    font-size: 13px;
                    line-height: 2.15384615;
                    min-height: 30px;
                    margin: 0;
                    padding: 0 10px;
                    border-width: 1px;
                    border-style: solid;
                    -webkit-appearance: none;
                    border-radius: 3px;
                    white-space: nowrap;
                    box-sizing: border-box;
                    font-weight: normal;
                }

                .raygansms-tools a {
                    cursor: pointer;
                }

                #TB_ajaxContent {
                    direction: rtl;
                    text-align: right !important;
                }

                #TB_ajaxContent p {
                    margin: 0;
                }

                #onlinepattern .pattern-message {
                    background: #dffde1;
                    padding: 10px;
                    border-radius: 5px;
                    border: 1px solid #bbb;
                    margin: 10px;
                    color: #666;
                }

                .raygansms-tools a {
                    display: block !important;
                    width: 150px;
                    text-align: center;
                }
            </style>
            <?php
        }

        private function getErrors($error)
        {
            $errorCodes = array('-1' => 'ارتباط با سامانه پیامک انجام نشد.', '0' => 'عملیات با موفقیت انجام شده است.', '1' => 'متن پیام خالی می باشد.', '2' => 'کاربر محدود گردیده است.', '3' => 'خط به شما تعلق ندارد.', '4' => 'گیرندگان خالی است.', '5' => 'اعتبار کافی نیست.', '7' => 'خط مورد نظر برای ارسال انبوه مناسب نمیباشد.', '9' => 'خط مورد نظر در این ساعت امکان ارسال ندارد. برای ارسال پیامک در ۲۴ ساعت شبانه روز از وب سرویس پترن استفاده نمایید', '98' => 'حداکثر تعداد گیرنده رعایت نشدهه است.', '99' => 'اپراتور خط ارسالی قطع می باشد.', '21' => 'پسوند فایل صوتی نامعتبر است.', '22' => 'سایز فایل صوتی نامعتبر است.', '23' => 'تعداد تالش در پیام صوتی نامعتبر است.', '100' => 'شماره مخاطب دفترچه تلفن نامعتبر می باشد.', '101' => 'شماره مخاطب در دفترچه تلفن وجود دارد.', '102' => 'شماره مخاطب با موفقیت در دفترچه تلفن ذخیره گردید.', '111' => 'حداکثر تعداد گیرنده برای ارسال پیام صوتی رعایت نشده است.', '131' => 'تعداد تالش در پیام صوتی باید یکبار باشد.', '132' => 'آدرس فایل صوتی وارد نگردیده است.', '301' => 'از حرف ویژه در نام کاربری استفاده گردیده است.', '302' => 'قیمت گذاری انجام نگریدهه است.', '303' => 'نام کاربری وارد نگردیده است.', '304' => 'نام کاربری قبال انتخاب گردیده است.', '305' => 'نام کاربری وارد نگردیده است.', '306' => 'کد ملی وارد نگردیده است.', '307' => 'کد ملی به خطا وارد شده است.', '308' => 'شماره شناسنامه نا معتبر است.', '309' => 'شماره شناسنامه وارد نگردیده است.', '310' => 'ایمیل کاربر وارد نگردیده است.', '311' => 'شماره تلفن وارد نگردیده است.', '312' => 'تلفن به درستی وارد نگردیده است.', '313' => 'آدرس شما وارد نگردیده است.', '314' => 'شماره موبایل را وارد نکرده اید.', '315' => 'شماره موبایل به نادرستی وارد گردیده است.', '316' => 'سطح دسترسی به نادرستی وارد گردیده است.', '317' => 'کلمه عبور وارد نگردیده است.', '455' => 'ارسال در آینده برای کد بالک ارسالی لغو شد.', '456' => 'کد بالک ارسالی نامعتبر است.', '458' => 'کد تیکت نامعتبر است.', '964' => 'شما دسترسی نمایندگی ندارید.', '962' => 'نام کاربری یا کلمه عبور نادرست می باشد.', '963' => 'دسترسی نامعتبر می باشد.', '971' => 'پترن ارسالی نامعتبر است.', '970' => 'پارامتر های ارسالی برای پترن نامعتبر است.', '972' => 'دریافت کننده برای ارسال پترن نامعتبر می باشد.', '992' => 'ارسال پیام از ساعت 8 تا 23 می باشد. برای ارسال پیامک در ۲۴ ساعت شبانه روز از وب سرویس پترن استفاده نمایید', '993' => 'دفترچه تلفن باید یک آرایه باشد', '994' => 'لطفا تصویری از کارت بانکی خود را از منو مدارک ارسال کنید', '995' => 'جهت ارسال با خطوط اشتراکی سامانه، لطفا شماره کارت بانکیه خود را به دلیل تکمیل فرایند  احراز هویت از بخش ارسال مدارک ثبت نمایید.', '996' => 'پترن فعال نیست.', '997' => 'شما اجازه ارسال از این پترن را ندارید.ه', '998' => 'کارت ملی یا کارت بانکی شما تایید نشده است.', '1001' => 'فرمت نام کاربری درست نمی باشد)حداقله ۵ کاراکتر، فقط حروف و اعداد(.', '1002' => 'گذر واژه خیلی ساده می باشد)حداقل ۸ کاراکتر بوده و نام کاربری،', 'ایمی' => ' و شماره موبایل در آن وجود نداشته باشد(.', '1003' => 'مشکل در ثبت، با پشتیبانی تماس بگیرید.', '1004' => 'مشکل در ثبت، با پشتیبانی تماس بگیرید.', '1005' => 'مشکل در ثبت، با پشتیبانی تماس بگیرید.', '1006' => 'تاریخ ارسال پیام برای گذشته می باشد، لطفا تاریخ ارسال پیام را به درستی وارد نمایید.ه',);
            return (isset($errorCodes[$error])) ? $errorCodes[$error] : 'اشکال تعریف نشده با کد :' . $error;
        }
    }
}
new pwoo_sms_raygansms();
function pwoo_sms_raygansms_activate()
{
    if (!function_exists('is_plugin_active_for_network')) {
        include_once(ABSPATH . '/wp-admin/includes/plugin.php');
    }
    if (current_user_can('activate_plugins') && !class_exists('WoocommerceIR_SMS_Gateways')) {
        deactivate_plugins(plugin_basename(__FILE__));
        $error_message = '<p>برای نصب این افزونه باید ابتدا افزونه <a href="https://fa.wordpress.org/plugins/persian-woocommerce-sms/">پیامک حرفه ای ووکامرس</a> را فعال نمایید.</p>';
        die($error_message);
    }
    update_option('pwoosms_ads_noticemelli', 1);
    $gateway_object = WoocommerceIR_SMS_Gateways::init();
    if (!method_exists($gateway_object, 'raygansms')) {
        $original_file = file_get_contents(WP_PLUGIN_DIR . '/persian-woocommerce-sms/includes/class-gateways.php');
        if (strpos($original_file, 'raygansms') === false) {
            $raygansms_function = '	public function raygansms( $datas ) {
		if(class_exists("pwoo_sms_raygansms")){
	        require_once WP_PLUGIN_DIR . "/persian-woocommerce-sms-raygansms/pwoo_sms_raygansms.php";
	    }
		if(class_exists("pwoo_sms_raygansms")){
			$raygansms = new pwoo_sms_raygansms;
			return $raygansms->SendSMS( $datas );
		}else{
			return "افزونه مکمل raygansms غیرفعال است. لطفا از صفحه افزونه ها فعال نمایید.";
		}
	}
}';
            $new_file = strrev(implode(strrev($raygansms_function), explode(strrev("}"), strrev($original_file), 2)));
            file_put_contents(WP_PLUGIN_DIR . '/persian-woocommerce-sms/includes/class-gateways.php', $new_file);
        }
    }
}

register_activation_hook(__FILE__, 'pwoo_sms_raygansms_activate');