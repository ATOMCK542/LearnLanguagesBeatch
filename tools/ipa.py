"""English IPA (Latin) for EN, RU and VI forms used in Triad packs."""
from __future__ import annotations

import re
from pathlib import Path

_EN_TABLE = """
a ə
an æn
the ðə
i aɪ
me miː
my maɪ
mine maɪn
myself maɪˈself
you juː
your jɔː
yours jɔːz
yourself jɔːˈself
yourselves jɔːˈselvz
he hiː
him hɪm
his hɪz
himself hɪmˈself
she ʃiː
her hɜː
hers hɜːz
herself hɜːˈself
it ɪt
its ɪts
itself ɪtˈself
we wiː
us ʌs
our ˈaʊə
ours ˈaʊəz
ourselves aʊəˈselvz
they ðeɪ
them ðem
their ðeə
theirs ðeəz
themselves ðəmˈselvz
this ðɪs
that ðæt
these ðiːz
those ðəʊz
who huː
whom huːm
whose huːz
which wɪtʃ
what wɒt
what's wɒts
where weə
when wen
why waɪ
how haʊ
some sʌm
any ˈeni
no nəʊ
every ˈevri
each iːtʃ
other ˈʌðə
someone ˈsʌmwʌn
somebody ˈsʌmbədi
anyone ˈeniwʌn
anybody ˈenibədi
everyone ˈevriwʌn
everybody ˈevribədi
nobody ˈnəʊbədi
no-one nəʊ wʌn
something ˈsʌmθɪŋ
anything ˈeniθɪŋ
everything ˈevriθɪŋ
nothing ˈnʌθɪŋ
somewhere ˈsʌmweə
anywhere ˈeniweə
everywhere ˈevriweə
nowhere ˈnəʊweə
there ðeə
here hɪə
am æm
is ɪz
are ɑː
was wɒz
were wɜː
be biː
been biːn
being ˈbiːɪŋ
have hæv
has hæz
had hæd
haven't ˈhævənt
hasn't ˈhæzənt
do duː
does dʌz
did dɪd
don't dəʊnt
doesn't ˈdʌzənt
didn't ˈdɪdənt
isn't ˈɪzənt
aren't ɑːnt
wasn't ˈwɒzənt
weren't wɜːnt
can kæn
can't kɑːnt
could kʊd
may meɪ
might maɪt
must mʌst
shall ʃæl
should ʃʊd
will wɪl
won't wəʊnt
would wʊd
ought ɔːt
need niːd
used juːst
not nɒt
never ˈnevə
to tə
from frɒm
with wɪð
for fɔː
of əv
in ɪn
on ɒn
at æt
about əˈbaʊt
by baɪ
into ˈɪntuː
out aʊt
up ʌp
down daʊn
over ˈəʊvə
under ˈʌndə
between bɪˈtwiːn
without wɪðˈaʊt
before bɪˈfɔː
after ˈɑːftə
during ˈdjʊərɪŋ
and ænd
but bʌt
or ɔː
because bɪˈkɒz
so səʊ
if ɪf
please pliːz
thanks θæŋks
thank θæŋk
sorry ˈsɒri
excuse ɪkˈskjuːz
welcome ˈwelkəm
you're jɔː
zero ˈzɪərəʊ
one wʌn
two tuː
three θriː
four fɔː
five faɪv
six sɪks
seven ˈsevən
eight eɪt
nine naɪn
ten ten
hundred ˈhʌndrəd
first fɜːst
second ˈsekənd
go ɡəʊ
goes ɡəʊz
went went
come kʌm
leave liːv
stay steɪ
live lɪv
work wɜːk
study ˈstʌdi
want wɒnt
like laɪk
love lʌv
know nəʊ
think θɪŋk
understand ˌʌndəˈstænd
remember rɪˈmembə
forget fəˈɡet
see siː
look lʊk
watch wɒtʃ
hear hɪə
listen ˈlɪsən
speak spiːk
say seɪ
tell tel
ask ɑːsk
answer ˈɑːnsə
call kɔːl
read riːd
write raɪt
eat iːt
drink drɪŋk
cook kʊk
buy baɪ
pay peɪ
sell sel
take teɪk
give ɡɪv
get ɡet
put pʊt
make meɪk
use juːz
open ˈəʊpən
close kləʊz
sit sɪt
stand stænd
walk wɔːk
wait weɪt
help help
start stɑːt
stop stɒp
sleep sliːp
wake weɪk
feel fiːl
try traɪ
find faɪnd
lose luːz
bring brɪŋ
wear weə
meet miːt
play pleɪ
show ʃəʊ
let let
keep kiːp
good ɡʊd
bad bæd
big bɪɡ
small smɔːl
new njuː
old əʊld
young jʌŋ
hot hɒt
cold kəʊld
warm wɔːm
cool kuːl
cheap tʃiːp
expensive ɪkˈspensɪv
easy ˈiːzi
hard hɑːd
fast fɑːst
slow sləʊ
near nɪə
far fɑː
closed kləʊzd
hungry ˈhʌŋɡri
thirsty ˈθɜːsti
tired ˈtaɪəd
happy ˈhæpi
sad sæd
beautiful ˈbjuːtɪfəl
delicious dɪˈlɪʃəs
right raɪt
wrong rɒŋ
clean kliːn
dirty ˈdɜːti
full fʊl
empty ˈempti
early ˈɜːli
late leɪt
busy ˈbɪzi
ready ˈredi
important ɪmˈpɔːtənt
same seɪm
different ˈdɪfrənt
next nekst
last lɑːst
hello həˈləʊ
hi haɪ
morning ˈmɔːnɪŋ
afternoon ˌɑːftəˈnuːn
evening ˈiːvnɪŋ
goodbye ɡʊdˈbaɪ
bye baɪ
yes jes
ok əʊˈkeɪ
okay əʊˈkeɪ
nice naɪs
name neɪm
anna ˈænə
fine faɪn
water ˈwɔːtə
rice raɪs
bread bred
coffee ˈkɒfi
tea tiː
milk mɪlk
meat miːt
fish fɪʃ
fruit fruːt
vegetable ˈvedʒtəbl
spicy ˈspaɪsi
bill bɪl
menu ˈmenjuː
table ˈteɪbəl
sugar ˈʃʊɡə
cup kʌp
waiter ˈweɪtə
bus bʌs
train treɪn
taxi ˈtæksi
airport ˈeəpɔːt
ticket ˈtɪkɪt
hotel həʊˈtel
left left
straight streɪt
size saɪz
color ˈkʌlə
colour ˈkʌlə
cash kæʃ
card kɑːd
receipt rɪˈsiːt
blue bluː
market ˈmɑːkɪt
just dʒʌst
looking ˈlʊkɪŋ
today təˈdeɪ
tomorrow təˈmɒrəʊ
yesterday ˈjestədeɪ
now naʊ
later ˈleɪtə
monday ˈmʌndeɪ
week wiːk
month mʌnθ
year jɪə
slowly ˈsləʊli
repeat rɪˈpiːt
english ˈɪŋɡlɪʃ
little ˈlɪtəl
congratulations kənˌɡrætʃʊˈleɪʃənz
luck lʌk
care keə
bless bles
problem ˈprɒbləm
course kɔːs
maybe ˈmeɪbi
home həʊm
student ˈstjuːdənt
time taɪm
people ˈpiːpəl
book bʊk
books bʊkz
cat kæt
food fuːd
toilet ˈtɔɪlət
station ˈsteɪʃən
train treɪn
bag bæɡ
car kɑː
rest rest
swim swɪm
eaten ˈiːtən
tired ˈtaɪəd
all ɔːl
both bəʊθ
many ˈmeni
much mʌtʃ
lot lɒt
few fjuː
kind kaɪnd
long lɒŋ
often ˈɒfən
old əʊld
come kʌm
mean miːn
does dʌz
zero-article ˈzɪərəʊ ˈɑːtɪkəl
plural ˈplʊərəl
abstract ˈæbstrækt
article ˈɑːtɪkəl
too tuː
also ˈɔːlsəʊ
very ˈveri
really ˈrɪəli
still stɪl
already ɔːlˈredi
yet jet
again əˈɡen
only ˈəʊnli
also ˈɔːlsəʊ
let's lets
lets lets
i'll aɪl
i'm aɪm
it's ɪts
that's ðæts
don't dəʊnt
didn't ˈdɪdənt
can't kɑːnt
won't wəʊnt
there's ðeəz
we'll wiːl
you'd juːd
could kʊd
would wʊd
should ʃʊd
cheaper ˈtʃiːpə
expensive ɪkˈspensɪv
blue bluː
"""

EN_IPA = {}
for _line in _EN_TABLE.splitlines():
    _line = _line.strip()
    if not _line:
        continue
    _w, _ipa = _line.split(None, 1)
    EN_IPA[_w] = _ipa

PHRASES = {
    "en": {
        "ought to": "ɔːt tə",
        "used to": "juːst tə",
        "a lot of": "ə lɒt əv",
        "a few": "ə fjuː",
        "a little": "ə ˈlɪtəl",
        "each other": "iːtʃ ˈʌðə",
        "no one": "nəʊ wʌn",
        "thank you": "θæŋk juː",
        "excuse me": "ɪkˈskjuːz miː",
        "you're welcome": "jɔː ˈwelkəm",
        "how much": "haʊ mʌtʃ",
        "how many": "haʊ ˈmeni",
        "how long": "haʊ lɒŋ",
        "how often": "haʊ ˈɒfən",
        "how far": "haʊ fɑː",
        "how old": "haʊ əʊld",
        "how come": "haʊ kʌm",
        "what time": "wɒt taɪm",
        "what kind of": "wɒt kaɪnd əv",
        "good morning": "ɡʊd ˈmɔːnɪŋ",
        "good afternoon": "ɡʊd ˌɑːftəˈnuːn",
        "good evening": "ɡʊd ˈiːvnɪŋ",
        "see you": "siː juː",
        "nice to meet you": "naɪs tə miːt juː",
        "of course": "əv kɔːs",
        "no problem": "nəʊ ˈprɒbləm",
        "good luck": "ɡʊd lʌk",
        "take care": "teɪk keə",
        "after you": "ˈɑːftə juː",
        "bless you": "bles juː",
        "to go": "tə ɡəʊ",
        "for here": "fɔː hɪə",
        "go straight": "ɡəʊ streɪt",
        "one hundred": "wʌn ˈhʌndrəd",
        "zero article (plural / abstract)": "ˈzɪərəʊ ˈɑːtɪkəl",
        "let's go": "lets ɡəʊ",
        "i'm fine, thanks": "aɪm faɪn θæŋks",
        "what's your name?": "wɒts jɔː neɪm",
        "what's this?": "wɒts ðɪs",
        "what's that?": "wɒts ðæt",
        "how are you?": "haʊ ɑː juː",
        "i don't understand": "aɪ dəʊnt ˌʌndəˈstænd",
        "do you speak english?": "duː juː spiːk ˈɪŋɡlɪʃ",
        "i speak a little": "aɪ spiːk ə ˈlɪtəl",
        "living room": "ˈlɪvɪŋ ruːm",
        "shoulder blade": "ˈʃəʊldə bleɪd",
        "post office": "pəʊst ˈɒfɪs",
        "ice cream": "aɪs kriːm",
        "blood pressure": "blʌd ˈpreʃə",
        "traffic light": "ˈtræfɪk laɪt",
        "mouse device": "maʊs dɪˈvaɪs",
        "light weight": "laɪt weɪt",
        "square shape": "skweə ʃeɪp",
        "id card": "aɪ diː kɑːd",
        "book a table": "bʊk ə ˈteɪbəl",
        "hot weather": "hɒt ˈweðə",
        "cold weather": "kəʊld ˈweðə",
        "warm weather": "wɔːm ˈweðə",
        "cool weather": "kuːl ˈweðə",
        "orange fruit": "ˈɒrɪndʒ fruːt",
        "cold illness": "kəʊld ˈɪlnəs",
        "hard material": "hɑːd məˈtɪəriəl",
        "last one": "lɑːst wʌn",
        "t-shirt": "ˈtiː ʃɜːt",
    },
    "ru": {
        "привет": "prʲɪˈvʲet",
        "я": "ja",
        "ты": "tɨ",
        "вы": "vɨ",
        "он": "on",
        "она": "ɐˈna",
        "мы": "mɨ",
        "они": "ɐˈnʲi",
        "спасибо": "spɐˈsʲibə",
        "пожалуйста": "pəˈʐaləstə",
        "кофе": "ˈkofʲe",
        "чай": "tɕaj",
        "вода": "vɐˈda",
        "хлеб": "xlʲep",
        "да": "da",
        "нет": "nʲet",
        "хорошо": "xərɐˈʂo",
        "сегодня": "sʲɪˈvodʲnʲə",
        "завтра": "ˈzavtrə",
        "вчера": "ftɕɪˈra",
        "сейчас": "sʲɪjˈtɕas",
        "как тебя зовут": "kak tʲɪˈbʲa zɐˈvut",
        "доброе утро": "ˈdobrəjə ˈutrə",
        "добрый день": "ˈdobrɨj dʲenʲ",
        "добрый вечер": "ˈdobrɨj ˈvʲetɕɪr",
        "до свидания": "də svʲɪˈdanʲɪjə",
        "пока": "pɐˈka",
        "извини": "ɪzvʲɪˈnʲi",
        "простите": "prɐsʲˈtʲitʲe",
        "пожалуйста": "pəˈʐaləstə",
        "спасибо": "spɐˈsʲibə",
        "я хочу кофе": "ja xɐˈtɕu ˈkofʲe",
        "сколько это стоит": "ˈskolʲkə ˈetə ˈstoɪt",
        "где": "ɡdʲe",
        "когда": "kəɡˈda",
        "почему": "pətɕɪˈmu",
        "что": "ʂto",
        "кто": "kto",
        "как": "kak",
    },
    "vi": {
        "tôi": "toj˧",
        "bạn": "ɓaːn˧ˀ",
        "xin chào": "sin˧ tɕaːw˧˥",
        "chào": "tɕaːw˧˥",
        "cảm ơn": "kaːm˧˩˧ ʔən˧",
        "không": "xowŋ˧",
        "có": "kɔ˧˥",
        "vâng": "vaŋ˧",
        "nước": "nɨək˧˥",
        "cà phê": "ka˨˩ fe˧",
        "trà": "tra˨˩",
        "xin lỗi": "sin˧ loj˧˩˧",
        "làm ơn": "laːm˨˩ ʔən˧",
        "gì": "zi˧˩",
        "tên": "ten˧",
        "không": "xowŋ˧",
        "của": "kuə˧˩˧",
        "là": "la˨˩",
        "một": "mot˧ˀ",
        "người": "ŋɨəj˨˩",
        "được": "ɗɨək˧ˀ",
        "này": "naj˨˩",
        "thế": "tʰe˧˥",
        "nào": "naːw˨˩",
        "ở": "əː˧˩˧",
        "đâu": "ɗəw˧",
        "và": "va˨˩",
        "nhưng": "ɲɨŋ˧",
        "rồi": "roj˨˩",
        "nhà": "ɲa˨˩",
        "đi": "ɗi˧",
        "ăn": "an˧",
        "uống": "uəŋ˧˥",
        "học": "hɔk˧ˀ",
        "nói": "nɔj˧˥",
        "biết": "ɓiet˧˥",
        "muốn": "muən˧˥",
        "thích": "tʰik˧˥",
        "yêu": "iəw˧",
        "làm": "laːm˨˩",
        "việc": "viək˧ˀ",
        "sách": "sajk˧˥",
        "tiền": "tiən˨˩",
        "xe": "sɛ˧",
        "tàu": "taːw˨˩",
        "vé": "vɛ˧˥",
        "chợ": "tɕəː˧ˀ",
        "màu": "maːw˨˩",
        "cỡ": "kəː˧˩˧",
        "thẻ": "tʰɛ˧˩˧",
        "hôm": "hom˧",
        "nay": "naj˧",
        "mai": "maːj˧",
        "qua": "kwaː˧",
        "giờ": "zəː˨˩",
        "tuần": "twən˨˩",
        "tháng": "tʰaŋ˧˥",
        "năm": "nam˧",
        "xin": "sin˧",
        "lỗi": "loj˧˩˧",
        "giúp": "zup˧˥",
        "chờ": "tɕəː˨˩",
        "chậm": "tɕəm˧ˀ",
        "khỏe": "xwɛ˧˩˧",
        "vui": "vuj˧",
        "mệt": "met˧ˀ",
        "đói": "ɗɔj˧˥",
        "khát": "xaːt˧˥",
        "ngon": "ŋɔn˧",
        "đắt": "ɗat˧˥",
        "rẻ": "zɛ˧˩˧",
        "nóng": "nɔŋ˧˥",
        "lạnh": "lajŋ˧ˀ",
        "tốt": "tot˧˥",
        "xấu": "saw˧˥",
        "to": "tɔ˧",
        "nhỏ": "ɲɔ˧˩˧",
        "mới": "məːj˧˥",
        "cũ": "ku˧˩˧",
        "đẹp": "ɗɛp˧ˀ",
        "dễ": "ze˧˩˧",
        "khó": "xɔ˧˥",
        "nhanh": "ɲajŋ˧",
        "gần": "ɡən˨˩",
        "xa": "saː˧",
        "đúng": "ɗuŋ˧˥",
        "sai": "saːj˧",
        "sạch": "sajk˧ˀ",
        "bẩn": "ɓən˧˩˧",
        "muộn": "muən˧ˀ",
        "sớm": "səːm˧˥",
        "bận": "ɓən˧ˀ",
        "sẵn": "sən˧˩˧",
        "sàng": "saːŋ˨˩",
    },
}

RU_CONS = {
    "б": "b", "в": "v", "г": "ɡ", "д": "d", "ж": "ʐ", "з": "z", "к": "k", "л": "l",
    "м": "m", "н": "n", "п": "p", "р": "r", "с": "s", "т": "t", "ф": "f", "х": "x",
    "ц": "ts", "ч": "tɕ", "ш": "ʂ", "щ": "ɕː",
}
RU_SOFT = {"ч", "щ"}
RU_IOTATED = {"е": "e", "ё": "o", "ю": "u", "я": "a"}
RU_VOWELS = {"а": "a", "э": "e", "ы": "ɨ", "у": "u", "о": "o", "и": "i"}

VI_ONSETS = [
    ("ngh", "ŋ"), ("ng", "ŋ"), ("nh", "ɲ"), ("gh", "ɣ"), ("gi", "z"), ("kh", "x"),
    ("ph", "f"), ("th", "tʰ"), ("tr", "tɕ"), ("qu", "kw"), ("ch", "tɕ"),
    ("gi", "z"), ("gh", "ɣ"),
]
VI_ONSET1 = {
    "b": "ɓ", "c": "k", "d": "z", "đ": "ɗ", "g": "ɣ", "h": "h", "k": "k", "l": "l",
    "m": "m", "n": "n", "p": "p", "q": "k", "r": "z", "s": "s", "t": "t", "v": "v",
    "x": "s",
}
VI_RIMES = {
    "a": "aː", "ai": "aːj", "ao": "aːw", "au": "aw", "ay": "aj",
    "ă": "a", "ăc": "ak", "ăm": "am", "ăn": "an", "ăng": "aŋ",
    "â": "ə", "âc": "ək", "âm": "əm", "ân": "ən", "âng": "əŋ", "ât": "ət", "ây": "əj",
    "e": "ɛ", "ec": "ɛk", "em": "ɛm", "en": "ɛn", "eng": "ɛŋ", "eo": "ɛw",
    "ê": "e", "êch": "ec", "êm": "em", "ên": "en", "ênh": "eɲ", "êu": "ew",
    "i": "i", "ia": "iə", "ich": "ik", "im": "im", "in": "in", "inh": "iɲ", "iu": "iw",
    "iêc": "iək", "iêm": "iəm", "iên": "iən", "iêng": "iəŋ", "iêt": "iət", "iêu": "iəw",
    "o": "ɔ", "oa": "waː", "oai": "waːj", "oao": "waːw", "oay": "waj",
    "oă": "wa", "oăn": "wan", "oăng": "waŋ",
    "oc": "ɔk", "oe": "wɛ", "oi": "ɔj", "om": "ɔm", "on": "ɔn", "ong": "ɔŋ",
    "ô": "o", "ôc": "ok", "ôi": "oj", "ôm": "om", "ôn": "on", "ông": "oŋ",
    "ơ": "əː", "ơi": "əːj", "ơm": "əːm", "ơn": "əːn",
    "u": "u", "ua": "uə", "uân": "wən", "uâng": "wəŋ", "uê": "we", "ui": "uj",
    "um": "um", "un": "un", "ung": "uŋ", "uy": "wi", "uya": "wiə", "uych": "wik",
    "uyên": "wiən", "uyu": "wiw",
    "uôc": "uək", "uôi": "uəj", "uôm": "uəm", "uôn": "uən", "uông": "uəŋ",
    "ư": "ɨ", "ưa": "ɨə", "ưi": "ɨj", "ưng": "ɨŋ", "ưu": "ɨw",
    "ươc": "ɨək", "ươi": "ɨəj", "ươm": "ɨəm", "ươn": "ɨən", "ương": "ɨəŋ", "ượt": "ɨət",
    "y": "i", "ych": "ik", "yê": "iə", "yên": "iən", "yêng": "iəŋ", "yêu": "iəw",
    "ac": "aːk", "ach": "ajk", "am": "aːm", "an": "aːn", "ang": "aːŋ", "anh": "ajŋ",
    "ao": "aːw", "ap": "aːp", "at": "aːt", "ay": "aj",
}
VI_TONE = {
    "sắc": "˧˥", "huyền": "˧˩", "hỏi": "˧˩˧", "ngã": "˧˥ˀ", "nặng": "˧ˀ", "ngang": "˧",
}
_TONE_CHAR = {}
for _base, _marked in {
    "a": "áàảãạ", "ă": "ắằẳẵặ", "â": "ấầẩẫậ",
    "e": "éèẻẽẹ", "ê": "ếềểễệ",
    "i": "íìỉĩị",
    "o": "óòỏõọ", "ô": "ốồổỗộ", "ơ": "ớờởỡợ",
    "u": "úùủũụ", "ư": "ứừửữự",
    "y": "ýỳỷỹỵ",
}.items():
    _names = ("sắc", "huyền", "hỏi", "ngã", "nặng")
    for _ch, _tone in zip(_marked, _names):
        _TONE_CHAR[_ch] = (_base, _tone)


def wrap(ipa: str) -> str:
    s = re.sub(r"\s+", " ", ipa.strip())
    if not s:
        return ""
    if s.startswith("/") and s.endswith("/") and len(s) > 2:
        return s
    return f"/{s}/"


def tokenize(text: str) -> list[str]:
    return [t for t in re.split(r"[^\w'`’-]+", text, flags=re.UNICODE) if t]


def _norm(text: str) -> str:
    return re.sub(r"\s+", " ", text.strip().lower())


def transcribe(lang: str, text: str, override: str = "") -> str:
    if override and override.strip():
        return wrap(override)
    key = _norm(text)
    phrase = PHRASES.get(lang, {}).get(key)
    if phrase:
        return wrap(phrase)
    tokens = tokenize(text)
    if not tokens:
        return wrap(key)
    bits = [_word(lang, tok) for tok in tokens]
    return wrap(" ".join(b for b in bits if b))


def _word(lang: str, token: str) -> str:
    raw = token.strip()
    low = raw.lower().replace("’", "'")
    if lang == "en":
        if low in EN_IPA:
            return EN_IPA[low]
        folded = low.replace("'", "")
        if folded in EN_IPA:
            return EN_IPA[folded]
        phrase = PHRASES["en"].get(low)
        if phrase:
            return phrase
        return _en_fallback(low)
    if lang == "ru":
        hit = PHRASES["ru"].get(low)
        if hit:
            return hit
        return _ru_word(low)
    if lang == "vi":
        hit = PHRASES["vi"].get(low)
        if hit:
            return hit
        return _vi_syllable(low)
    return low


_CMU: dict[str, str] | None = None

_ARPABET = {
    "AA": "ɑ", "AE": "æ", "AH": "ʌ", "AO": "ɔ", "AW": "aʊ", "AY": "aɪ",
    "B": "b", "CH": "tʃ", "D": "d", "DH": "ð", "EH": "ɛ", "ER": "ɝ",
    "EY": "eɪ", "F": "f", "G": "ɡ", "HH": "h", "IH": "ɪ", "IY": "i",
    "JH": "dʒ", "K": "k", "L": "l", "M": "m", "N": "n", "NG": "ŋ",
    "OW": "oʊ", "OY": "ɔɪ", "P": "p", "R": "ɹ", "S": "s", "SH": "ʃ",
    "T": "t", "TH": "θ", "UH": "ʊ", "UW": "u", "V": "v", "W": "w",
    "Y": "j", "Z": "z", "ZH": "ʒ",
}


def _cmu() -> dict[str, str]:
    """CMUdict (cmusphinx/cmudict, BSD) as American IPA. Curated EN_IPA still wins."""
    global _CMU
    if _CMU is not None:
        return _CMU
    path = Path(__file__).resolve().parent / "data" / "cmudict.dict"
    table: dict[str, str] = {}
    if path.exists():
        for raw in path.read_text(encoding="latin-1").splitlines():
            if not raw or raw.startswith(";"):
                continue
            word, _, pron = raw.partition(" ")
            if not pron or "(" in word:
                continue
            key = word.lower()
            if key not in table:
                ipa = _arpabet_to_ipa(pron)
                if ipa:
                    table[key] = ipa
    _CMU = table
    return table


def _arpabet_to_ipa(pron: str) -> str:
    phones = pron.split()
    syllables: list[tuple[list[str], int]] = []
    current: list[str] = []
    for phone in phones:
        stress = None
        base = phone
        if phone[-1].isdigit():
            stress = int(phone[-1])
            base = phone[:-1]
        if base not in _ARPABET:
            return ""
        current.append(base)
        if stress is not None:
            syllables.append((current, stress))
            current = []
    if not syllables:
        return ""
    if current:
        syllables[-1] = (syllables[-1][0] + current, syllables[-1][1])
    out: list[str] = []
    for syl, stress in syllables:
        if stress == 1:
            out.append("ˈ")
        elif stress == 2:
            out.append("ˌ")
        for ph in syl:
            if ph == "AH" and stress == 0:
                out.append("ə")
            elif ph == "ER" and stress == 0:
                out.append("ɚ")
            else:
                out.append(_ARPABET[ph])
    return "".join(out)


def _en_fallback(word: str) -> str:
    hit = _cmu().get(word.lower().replace("’", "'"))
    if hit:
        return hit
    return word


def _ru_word(word: str) -> str:
    out: list[str] = []
    i = 0
    chars = list(word)
    while i < len(chars):
        ch = chars[i]
        nxt = chars[i + 1] if i + 1 < len(chars) else ""
        prev = chars[i - 1] if i else ""
        if ch in "/":
            i += 1
            continue
        if ch == "ь":
            i += 1
            continue
        if ch == "ъ":
            i += 1
            continue
        if ch in RU_IOTATED:
            vowel = RU_IOTATED[ch]
            palatal_prev = prev in RU_CONS or prev in RU_SOFT
            start = i == 0 or prev in "ъь" or prev in RU_VOWELS or prev in RU_IOTATED or prev in "й"
            if start:
                out.append("j" + vowel)
            elif palatal_prev:
                if out:
                    last = out[-1]
                    if last and last[-1] not in "ʲ" and last not in {"tɕ", "ɕː", "ʐ", "ʂ", "ts", "j"}:
                        out[-1] = last + "ʲ"
                out.append(vowel)
            else:
                out.append(vowel)
            i += 1
            continue
        if ch == "й":
            out.append("j")
            i += 1
            continue
        if ch in RU_VOWELS:
            out.append(RU_VOWELS[ch])
            i += 1
            continue
        if ch in RU_CONS:
            ipa = RU_CONS[ch]
            if nxt == "ь" or nxt in RU_IOTATED or nxt == "и":
                if ipa not in {"tɕ", "ɕː", "ʐ", "ʂ", "ts"}:
                    ipa += "ʲ"
            out.append(ipa)
            i += 1
            continue
        if ch.isalpha():
            out.append(ch)
        i += 1
    return "".join(out)


def _strip_vi_tone(syl: str) -> tuple[str, str]:
    tone = "ngang"
    chars = []
    for ch in syl:
        if ch in _TONE_CHAR:
            base, tone = _TONE_CHAR[ch]
            chars.append(base)
        else:
            chars.append(ch)
    return "".join(chars), tone


def _vi_syllable(syl: str) -> str:
    if not syl:
        return ""
    bare, tone = _strip_vi_tone(syl.lower())
    onset = ""
    rest = bare
    matched = ""
    for letters, ipa in VI_ONSETS:
        if rest.startswith(letters):
            onset = ipa
            rest = rest[len(letters) :]
            matched = letters
            break
    else:
        if rest[:1] in VI_ONSET1:
            onset = VI_ONSET1[rest[0]]
            rest = rest[1:]
    if rest == "" and matched == "gi":
        rest = "i"
    elif rest == "" and matched == "qu":
        rest = "u"
    rime = VI_RIMES.get(rest)
    if rime is None:
        rime = "".join(VI_RIMES.get(ch, ch) for ch in rest)
    return f"{onset}{rime}{VI_TONE[tone]}"


def looks_like_orthography(text: str, ipa: str) -> bool:
    """True when IPA is just the original phrase in slashes (the old generator bug)."""
    inner = ipa.strip().strip("/")
    if " " not in text.strip() and " " not in inner:
        return False
    compact_text = re.sub(r"[^\w]+", "", text, flags=re.UNICODE).casefold()
    compact_ipa = re.sub(r"[^\w]+", "", inner, flags=re.UNICODE).casefold()
    return bool(compact_text) and compact_text == compact_ipa
