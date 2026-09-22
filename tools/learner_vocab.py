"""Extra learner lemmas: closed sets plus tools/data/vocab/*.tsv (en|ru|vi)."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parent
VOCAB = ROOT / "data" / "vocab"

EN_ONES = ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"]
EN_TEENS = [
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen",
    "sixteen", "seventeen", "eighteen", "nineteen",
]
EN_TENS = ["", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"]
RU_ONES = ["ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"]
RU_TEENS = [
    "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать",
    "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать",
]
RU_TENS = ["", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"]
RU_HUNDREDS = [
    "", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот",
]
VI_ONES = ["không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"]
VI_TENS_WORD = ["", "", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"]


def _vi_cardinal(n: int) -> str:
    if n < 10:
        return VI_ONES[n]
    if n < 20:
        if n == 10:
            return "mười"
        if n == 15:
            return "mười lăm"
        return f"mười {VI_ONES[n - 10]}"
    if n < 100:
        tens, ones = divmod(n, 10)
        head = f"{VI_TENS_WORD[tens]} mươi"
        if ones == 0:
            return head
        if ones == 1:
            return f"{head} mốt"
        if ones == 5:
            return f"{head} lăm"
        if ones == 4:
            return f"{head} tư"
        return f"{head} {VI_ONES[ones]}"
    if n < 1000:
        hundreds, rest = divmod(n, 100)
        head = f"{VI_ONES[hundreds]} trăm"
        if rest == 0:
            return head
        if rest < 10:
            return f"{head} lẻ {_vi_cardinal(rest)}"
        return f"{head} {_vi_cardinal(rest)}"
    if n == 1000:
        return "một nghìn"
    if n == 10000:
        return "mười nghìn"
    return str(n)


def _ru_cardinal(n: int) -> str:
    if n < 10:
        return RU_ONES[n]
    if n < 20:
        return RU_TEENS[n - 10]
    if n < 100:
        tens, ones = divmod(n, 10)
        if ones == 0:
            return RU_TENS[tens]
        return f"{RU_TENS[tens]} {RU_ONES[ones]}"
    if n < 1000:
        hundreds, rest = divmod(n, 100)
        if rest == 0:
            return RU_HUNDREDS[hundreds]
        return f"{RU_HUNDREDS[hundreds]} {_ru_cardinal(rest)}"
    if n == 1000:
        return "тысяча"
    if n == 10000:
        return "десять тысяч"
    return str(n)


def _en_cardinal(n: int) -> str:
    if n < 10:
        return EN_ONES[n]
    if n < 20:
        return EN_TEENS[n - 10]
    if n < 100:
        tens, ones = divmod(n, 10)
        if ones == 0:
            return EN_TENS[tens]
        return f"{EN_TENS[tens]}-{EN_ONES[ones]}"
    if n < 1000:
        hundreds, rest = divmod(n, 100)
        head = f"{EN_ONES[hundreds]} hundred"
        if rest == 0:
            return head
        return f"{head} {_en_cardinal(rest)}"
    if n == 1000:
        return "one thousand"
    if n == 10000:
        return "ten thousand"
    return str(n)


_ORD_EN = {
    1: "first", 2: "second", 3: "third", 4: "fourth", 5: "fifth",
    6: "sixth", 7: "seventh", 8: "eighth", 9: "ninth", 10: "tenth",
    11: "eleventh", 12: "twelfth", 13: "thirteenth", 14: "fourteenth", 15: "fifteenth",
    16: "sixteenth", 17: "seventeenth", 18: "eighteenth", 19: "nineteenth", 20: "twentieth",
    30: "thirtieth",
}
_ORD_RU = {
    1: "первый", 2: "второй", 3: "третий", 4: "четвёртый", 5: "пятый",
    6: "шестой", 7: "седьмой", 8: "восьмой", 9: "девятый", 10: "десятый",
    11: "одиннадцатый", 12: "двенадцатый", 13: "тринадцатый", 14: "четырнадцатый", 15: "пятнадцатый",
    16: "шестнадцатый", 17: "семнадцатый", 18: "восемнадцатый", 19: "девятнадцатый", 20: "двадцатый",
    30: "тридцатый",
}
_ORD_VI = {
    1: "thứ nhất", 2: "thứ hai", 3: "thứ ba", 4: "thứ tư", 5: "thứ năm",
    6: "thứ sáu", 7: "thứ bảy", 8: "thứ tám", 9: "thứ chín", 10: "thứ mười",
    11: "thứ mười một", 12: "thứ mười hai", 13: "thứ mười ba", 14: "thứ mười bốn", 15: "thứ mười lăm",
    16: "thứ mười sáu", 17: "thứ mười bảy", 18: "thứ mười tám", 19: "thứ mười chín", 20: "thứ hai mươi",
    30: "thứ ba mươi",
}


def _ordinal(n: int) -> tuple[str, str, str]:
    if n in _ORD_EN:
        return _ORD_EN[n], _ORD_RU[n], _ORD_VI[n]
    base = _en_cardinal(n)
    if base.endswith("y"):
        en = base[:-1] + "ieth"
    else:
        en = base + "th"
    return en, f"{_ru_cardinal(n)}-й", f"thứ {_vi_cardinal(n)}"


COUNTRIES = """
Russia|Россия|Nga
Vietnam|Вьетнам|Việt Nam
China|Китай|Trung Quốc
Japan|Япония|Nhật Bản
Korea|Корея|Hàn Quốc
Thailand|Таиланд|Thái Lan
Laos|Лаос|Lào
Cambodia|Камбоджа|Campuchia
Singapore|Сингапур|Singapore
Malaysia|Малайзия|Malaysia
Indonesia|Индонезия|Indonesia
Philippines|Филиппины|Philippines
India|Индия|Ấn Độ
France|Франция|Pháp
Germany|Германия|Đức
Italy|Италия|Ý
Spain|Испания|Tây Ban Nha
Portugal|Португалия|Bồ Đào Nha
United Kingdom|Великобритания|Vương quốc Anh
England|Англия|Anh
Ireland|Ирландия|Ireland
United States|США|Hoa Kỳ
Canada|Канада|Canada
Mexico|Мексика|Mexico
Brazil|Бразилия|Brazil
Australia|Австралия|Úc
New Zealand|Новая Зеландия|New Zealand
Egypt|Египет|Ai Cập
Turkey|Турция|Thổ Nhĩ Kỳ
Greece|Греция|Hy Lạp
Poland|Польша|Ba Lan
Ukraine|Украина|Ukraina
Finland|Финляндия|Phần Lan
Sweden|Швеция|Thụy Điển
Norway|Норвегия|Na Uy
Denmark|Дания|Đan Mạch
Netherlands|Нидерланды|Hà Lan
Belgium|Бельгия|Bỉ
Switzerland|Швейцария|Thụy Sĩ
Austria|Австрия|Áo
Czech Republic|Чехия|Séc
Hungary|Венгрия|Hungary
Romania|Румыния|Romania
Bulgaria|Болгария|Bulgaria
Serbia|Сербия|Serbia
Croatia|Хорватия|Croatia
Israel|Израиль|Israel
Saudi Arabia|Саудовская Аравия|Ả Rập Xê Út
United Arab Emirates|ОАЭ|Các Tiểu vương quốc Ả Rập Thống nhất
Iran|Иран|Iran
Iraq|Ирак|Iraq
South Africa|ЮАР|Nam Phi
Nigeria|Нигерия|Nigeria
Kenya|Кения|Kenya
Morocco|Марокко|Ma Rốc
Argentina|Аргентина|Argentina
Chile|Чили|Chile
Colombia|Колумбия|Colombia
Peru|Перу|Peru
Cuba|Куба|Cuba
Mongolia|Монголия|Mông Cổ
Kazakhstan|Казахстан|Kazakhstan
Uzbekistan|Узбекистан|Uzbekistan
Myanmar|Мьянма|Myanmar
Taiwan|Тайвань|Đài Loan
Hong Kong|Гонконг|Hồng Kông
"""

NATIONALITIES = """
Russian|русский|người Nga
Vietnamese|вьетнамский|người Việt
Chinese|китайский|người Trung Quốc
Japanese|японский|người Nhật
Korean|корейский|người Hàn
Thai|тайский|người Thái
French|французский|người Pháp
German|немецкий|người Đức
Italian|итальянский|người Ý
Spanish|испанский|người Tây Ban Nha
Portuguese|португальский|người Bồ Đào Nha
British|британский|người Anh
American|американский|người Mỹ
Canadian|канадский|người Canada
Australian|австралийский|người Úc
Indian|индийский|người Ấn
Brazilian|бразильский|người Brazil
Mexican|мексиканский|người Mexico
Turkish|турецкий|người Thổ Nhĩ Kỳ
Polish|польский|người Ba Lan
Ukrainian|украинский|người Ukraina
Swedish|шведский|người Thụy Điển
Dutch|нидерландский|người Hà Lan
Swiss|швейцарский|người Thụy Sĩ
Greek|греческий|người Hy Lạp
Egyptian|египетский|người Ai Cập
Israeli|израильский|người Israel
Indonesian|индонезийский|người Indonesia
Malaysian|малайзийский|người Malaysia
Filipino|филиппинский|người Philippines
"""

LANGUAGES = """
English language|английский язык|tiếng Anh
Russian language|русский язык|tiếng Nga
Vietnamese language|вьетнамский язык|tiếng Việt
Chinese language|китайский язык|tiếng Trung
Japanese language|японский язык|tiếng Nhật
Korean language|корейский язык|tiếng Hàn
French language|французский язык|tiếng Pháp
German language|немецкий язык|tiếng Đức
Spanish language|испанский язык|tiếng Tây Ban Nha
Italian language|итальянский язык|tiếng Ý
Portuguese language|португальский язык|tiếng Bồ Đào Nha
Thai language|тайский язык|tiếng Thái
Arabic language|арабский язык|tiếng Ả Rập
Hindi language|хинди|tiếng Hindi
"""


def _block(text: str) -> list[tuple[str, str, str]]:
    rows = []
    for raw in text.strip().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        en, ru, vi = [p.strip() for p in line.split("|", 2)]
        rows.append((en, ru, vi))
    return rows


def _numbers() -> list[tuple[str, str, str]]:
    rows = []
    for n in list(range(0, 101)) + [200, 300, 400, 500, 600, 700, 800, 900, 1000, 10000]:
        rows.append((_en_cardinal(n), _ru_cardinal(n), _vi_cardinal(n)))
    for n in range(1, 21):
        rows.append(_ordinal(n))
    rows.append(_ordinal(30))
    return rows


def _files() -> list[tuple[str, str, str]]:
    rows = []
    if not VOCAB.exists():
        return rows
    for path in sorted(VOCAB.glob("*.tsv")):
        rows.extend(_block(path.read_text(encoding="utf-8")))
    return rows


def learner_rows() -> list[tuple[str, str, str]]:
    rows = []
    rows.extend(_numbers())
    rows.extend(_block(COUNTRIES))
    rows.extend(_block(NATIONALITIES))
    rows.extend(_block(LANGUAGES))
    rows.extend(_files())
    seen = set()
    out = []
    for en, ru, vi in rows:
        key = (en.casefold(), ru.casefold())
        if key in seen or not en or not ru or not vi:
            continue
        if not any("а" <= ch <= "я" or "А" <= ch <= "Я" or ch in "ёЁ" for ch in ru):
            continue
        seen.add(key)
        out.append((en, ru, vi))
    return out
