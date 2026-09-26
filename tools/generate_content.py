#!/usr/bin/env python3
"""Generate trilingual content packs and the closed-class checklist."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "content-src"
PACKS = SRC / "packs"
ASSETS = ROOT / "app/src/main/assets/content"
sys.path.insert(0, str(Path(__file__).resolve().parent))
from ipa import looks_like_orthography, transcribe  # noqa: E402
from freq_lexicon import extra_specs  # noqa: E402
from function_lexicon import (  # noqa: E402
    CONJUNCTIONS,
    DETERMINERS,
    PARTICLES,
    PREPOSITIONS,
    PREP_PHRASES,
    aux_extra,
    lines as lex_lines,
    prep_id,
    slug,
)
from learner_vocab import learner_rows  # noqa: E402


def loc(en: str, ru: str, vi: str) -> dict:
    return {"en": en, "ru": ru, "vi": vi}


def concept(
    cid: str,
    kind: str,
    theme: str,
    tags: list[str],
    en: str,
    ru: str,
    vi: str,
    *,
    en_ipa: str = "",
    ru_ipa: str = "",
    vi_ipa: str = "",
    tones: list[str] | None = None,
    grammar: dict | None = None,
    level: int = 0,
    uses: list[str] | None = None,
    hint_en_ru: str = "",
    hint_en_vi: str = "",
    hint_ru_en: str = "",
    hint_ru_vi: str = "",
    hint_vi_ru: str = "",
    hint_vi_en: str = "",
) -> dict:
    g = grammar or loc("", "", "")
    row = {
        "id": cid,
        "kind": kind,
        "theme": theme,
        "tags": tags,
        "grammar": g,
        "texts": {
            "en": {
                "text": en,
                "ipa": transcribe("en", en, en_ipa),
                "hints": {k: v for k, v in {"ru": hint_en_ru or en, "vi": hint_en_vi or en}.items() if v},
            },
            "ru": {
                "text": ru,
                "ipa": transcribe("ru", ru, ru_ipa),
                "hints": {k: v for k, v in {"en": hint_ru_en or ru, "vi": hint_ru_vi or ru}.items() if v},
            },
            "vi": {
                "text": vi,
                "ipa": transcribe("vi", vi, vi_ipa),
                "tones": tones or [],
                "hints": {k: v for k, v in {"ru": hint_vi_ru or vi, "en": hint_vi_en or vi}.items() if v},
            },
        },
    }
    if uses:
        row["level"] = level
        row["uses"] = uses
    return row


def pack(theme_id: str, order: int, title: dict, concepts: list, kind: str = "unit", description: dict | None = None) -> dict:
    return {
        "theme": {
            "id": theme_id,
            "kind": kind,
            "order": order,
            "title": title,
            "description": description or loc("", "", ""),
        },
        "concepts": concepts,
    }


def write_pack(name: str, data: dict) -> None:
    PACKS.mkdir(parents=True, exist_ok=True)
    ASSETS.mkdir(parents=True, exist_ok=True)
    text = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    (PACKS / name).write_text(text, encoding="utf-8")
    (ASSETS / name).write_text(text, encoding="utf-8")


def pronouns() -> list[dict]:
    rows = [
        ("pron.i", "I", "я", "tôi", "aɪ", "ja", "toj", ["ngang"]),
        ("pron.you", "you", "ты / вы", "bạn", "ju", "tɨ", "ɓaːn", ["nặng"]),
        ("pron.he", "he", "он", "anh ấy", "hiː", "on", "aj ʔəj", ["ngang", "hỏi"]),
        ("pron.she", "she", "она", "cô ấy", "ʃiː", "ɐˈna", "ko ʔəj", ["ngang", "hỏi"]),
        ("pron.it", "it", "оно / это", "nó", "ɪt", "ɐˈno", "nɔ", ["sắc"]),
        ("pron.we", "we", "мы", "chúng tôi", "wiː", "mɨ", "cuŋ toj", ["sắc", "ngang"]),
        ("pron.they", "they", "они", "họ", "ðeɪ", "ɐˈnʲi", "hɔ", ["nặng"]),
        ("pron.me", "me", "мне / меня", "tôi (tân ngữ)", "miː", "mnʲe", "toj", ["ngang"]),
        ("pron.you_obj", "you", "тебе / вас", "bạn", "ju", "tʲɪˈbʲe", "ɓaːn", ["nặng"]),
        ("pron.him", "him", "ему / его", "anh ấy", "hɪm", "jɪˈmu", "aj ʔəj", ["ngang", "hỏi"]),
        ("pron.her_obj", "her", "ей / её", "cô ấy", "hɜː", "jej", "ko ʔəj", ["ngang", "hỏi"]),
        ("pron.us", "us", "нам / нас", "chúng tôi", "ʌs", "nam", "cuŋ toj", ["sắc", "ngang"]),
        ("pron.them", "them", "им / их", "họ", "ðem", "im", "hɔ", ["nặng"]),
        ("pron.my", "my", "мой", "của tôi", "maɪ", "moj", "kuə toj", ["hỏi", "ngang"]),
        ("pron.your", "your", "твой / ваш", "của bạn", "jɔː", "tvoj", "kuə ɓaːn", ["hỏi", "nặng"]),
        ("pron.his_adj", "his", "его", "của anh ấy", "hɪz", "jɪˈvo", "kuə aj ʔəj", ["hỏi", "ngang", "hỏi"]),
        ("pron.her_adj", "her", "её", "của cô ấy", "hɜː", "jɪˈjo", "kuə ko ʔəj", ["hỏi", "ngang", "hỏi"]),
        ("pron.its", "its", "его (неодуш.)", "của nó", "ɪts", "jɪˈvo", "kuə nɔ", ["hỏi", "sắc"]),
        ("pron.our", "our", "наш", "của chúng tôi", "aʊə", "naʂ", "kuə cuŋ toj", ["hỏi", "sắc", "ngang"]),
        ("pron.their", "their", "их", "của họ", "ðeə", "ix", "kuə hɔ", ["hỏi", "nặng"]),
        ("pron.mine", "mine", "мой (без сущ.)", "của tôi", "maɪn", "moj", "kuə toj", ["hỏi", "ngang"]),
        ("pron.yours", "yours", "твой (без сущ.)", "của bạn", "jɔːz", "tvoj", "kuə ɓaːn", ["hỏi", "nặng"]),
        ("pron.his_pron", "his", "его (без сущ.)", "của anh ấy", "hɪz", "jɪˈvo", "kuə aj ʔəj", ["hỏi", "ngang", "hỏi"]),
        ("pron.hers", "hers", "её (без сущ.)", "của cô ấy", "hɜːz", "jɪˈjo", "kuə ko ʔəj", ["hỏi", "ngang", "hỏi"]),
        ("pron.ours", "ours", "наш (без сущ.)", "của chúng tôi", "aʊəz", "naʂ", "kuə cuŋ toj", ["hỏi", "sắc", "ngang"]),
        ("pron.theirs", "theirs", "их (без сущ.)", "của họ", "ðeəz", "ix", "kuə hɔ", ["hỏi", "nặng"]),
        ("pron.myself", "myself", "себя / сам", "bản thân tôi", "maɪˈself", "sʲɪˈbʲa", "ɓaːn tʰən toj", ["nặng", "ngang", "ngang"]),
        ("pron.yourself", "yourself", "себя (ты)", "bản thân bạn", "jɔːˈself", "sʲɪˈbʲa", "ɓaːn tʰən ɓaːn", ["nặng", "ngang", "nặng"]),
        ("pron.himself", "himself", "себя (он)", "bản thân anh ấy", "hɪmˈself", "sʲɪˈbʲa", "ɓaːn tʰən aj ʔəj", ["nặng", "ngang", "ngang", "hỏi"]),
        ("pron.herself", "herself", "себя (она)", "bản thân cô ấy", "hɜːˈself", "sʲɪˈbʲa", "ɓaːn tʰən ko ʔəj", ["nặng", "ngang", "ngang", "hỏi"]),
        ("pron.itself", "itself", "само", "bản thân nó", "ɪtˈself", "ˈsamə", "ɓaːn tʰən nɔ", ["nặng", "ngang", "sắc"]),
        ("pron.ourselves", "ourselves", "себя (мы)", "bản thân chúng tôi", "aʊəˈselvz", "sʲɪˈbʲa", "ɓaːn tʰən cuŋ toj", ["nặng", "ngang", "sắc", "ngang"]),
        ("pron.yourselves", "yourselves", "себя (вы)", "bản thân các bạn", "jɔːˈselvz", "sʲɪˈbʲa", "ɓaːn tʰən kak ɓaːn", ["nặng", "ngang", "sắc", "nặng"]),
        ("pron.themselves", "themselves", "себя (они)", "bản thân họ", "ðəmˈselvz", "sʲɪˈbʲa", "ɓaːn tʰən hɔ", ["nặng", "ngang", "nặng"]),
        ("pron.this", "this", "этот / это", "này", "ðɪs", "ˈetət", "naj", ["huyền"]),
        ("pron.that", "that", "тот / то", "kia / đó", "ðæt", "tot", "kiə", ["ngang"]),
        ("pron.these", "these", "эти", "những cái này", "ðiːz", "ˈetʲɪ", "ɲɨŋ kaj naj", ["sắc", "sắc", "huyền"]),
        ("pron.those", "those", "те", "những cái kia", "ðəʊz", "tʲe", "ɲɨŋ kaj kiə", ["sắc", "sắc", "ngang"]),
        ("pron.some", "some", "несколько / некоторые", "một vài", "sʌm", "nʲɪsˈkolʲkə", "mot vaj", ["nặng", "huyền"]),
        ("pron.any", "any", "какой-либо / любой", "bất kỳ", "ˈeni", "lʲʊˈboj", "ɓət ki", ["sắc", "ngang"]),
        ("pron.no_det", "no", "никакой / нет", "không", "nəʊ", "nʲɪkɐˈkoj", "xom", ["ngang"]),
        ("pron.every", "every", "каждый", "mỗi", "ˈevri", "ˈkaʐdɨj", "moj", ["hỏi"]),
        ("pron.someone", "someone", "кто-то", "ai đó", "ˈsʌmwʌn", "ˈkto-tə", "aj ɗɔ", ["ngang", "sắc"]),
        ("pron.anyone", "anyone", "кто-нибудь", "bất kỳ ai", "ˈeniwʌn", "kto-nʲɪˈbutʲ", "ɓət ki aj", ["sắc", "ngang", "ngang"]),
        ("pron.everyone", "everyone", "все", "mọi người", "ˈevriwʌn", "fsʲe", "mɔj ŋɨəj", ["nặng", "hời"]),
        ("pron.no_one", "no one", "никто", "không ai", "nəʊ wʌn", "nʲɪkˈto", "xom aj", ["ngang", "ngang"]),
        ("pron.somebody", "somebody", "кто-то", "ai đó", "ˈsʌmbədi", "ˈkto-tə", "aj ɗɔ", ["ngang", "sắc"]),
        ("pron.anybody", "anybody", "кто-нибудь", "bất kỳ ai", "ˈenibədi", "kto-nʲɪˈbutʲ", "ɓət ki aj", ["sắc", "ngang", "ngang"]),
        ("pron.everybody", "everybody", "все", "mọi người", "ˈevribədi", "fsʲe", "mɔj ŋɨəj", ["nặng", "hời"]),
        ("pron.nobody", "nobody", "никто", "không ai", "ˈnəʊbədi", "nʲɪkˈto", "xom aj", ["ngang", "ngang"]),
        ("pron.something", "something", "что-то", "cái gì đó", "ˈsʌmθɪŋ", "ˈʂto-tə", "kaj zi ɗɔ", ["sắc", "hời", "sắc"]),
        ("pron.anything", "anything", "что-нибудь", "bất cứ gì", "ˈeniθɪŋ", "ʂto-nʲɪˈbutʲ", "ɓət kɨ zi", ["sắc", "sắc", "hời"]),
        ("pron.everything", "everything", "всё", "mọi thứ", "ˈevriθɪŋ", "fsʲo", "mɔj tʰɨ", ["nặng", "sắc"]),
        ("pron.nothing", "nothing", "ничего", "không gì", "ˈnʌθɪŋ", "nʲɪtɕɪˈvo", "xom zi", ["ngang", "hời"]),
        ("pron.somewhere", "somewhere", "где-то", "đâu đó", "ˈsʌmweə", "ˈɡdʲe-tə", "ɗəw ɗɔ", ["ngang", "sắc"]),
        ("pron.anywhere", "anywhere", "где-нибудь", "bất cứ đâu", "ˈeniweə", "ɡdʲe-nʲɪˈbutʲ", "ɓət kɨ ɗəw", ["sắc", "sắc", "ngang"]),
        ("pron.everywhere", "everywhere", "везде", "mọi nơi", "ˈevriweə", "vʲɪzˈdʲe", "mɔj nəj", ["nặng", "ngang"]),
        ("pron.nowhere", "nowhere", "нигде", "không đâu", "ˈnəʊweə", "nʲɪɡˈdʲe", "xom ɗəw", ["ngang", "ngang"]),
        ("pron.each_other", "each other", "друг друга", "nhau", "iːtʃ ˈʌðə", "druk ˈdruɡə", "ɲaw", ["ngang"]),
        ("pron.rel_who", "who", "который (люди)", "người mà", "huː", "kɐˈtorɨj", "ŋɨəj ma", ["hời", "mà"]),
        ("pron.rel_which", "which", "который (вещи)", "mà / cái nào", "wɪtʃ", "kɐˈtorɨj", "ma", ["mà"]),
        ("pron.rel_that", "that", "который / что", "mà", "ðæt", "ʂto", "ma", ["mà"]),
        ("pron.rel_whose", "whose", "чей / которого", "của ai / mà của", "huːz", "tɕej", "kuə aj", ["hỏi", "ngang"]),
        ("pron.someone_body", "somebody", "кто-то", "ai đó", "ˈsʌmbədi", "ˈkto-tə", "aj ɗɔ", ["ngang", "sắc"]),
    ]
    out = []
    g = loc("Pronoun form.", "Форма местоимения.", "Đại từ.")
    for cid, en, ru, vi, eipa, ripa, vipa, tones in rows:
        tag = "pronoun"
        out.append(concept(cid, "word", "pronouns", [tag], en, ru, vi, en_ipa=eipa, ru_ipa=ripa, vi_ipa=vipa, tones=tones, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    out.append(concept("pron.dummy_it", "sentence", "pronouns", ["pronoun"], "It is cold", "Холодно", "Trời lạnh",
                       en_ipa="/ɪt ɪz kəʊld/", grammar=loc("Dummy it.", "Формальное it.", "It giả."),
                       hint_en_ru="ит из коулд", hint_en_vi="it iz kold", hint_ru_en="kholodno", hint_ru_vi="kholodno",
                       hint_vi_ru="тьой лань", hint_vi_en="troi lanh", tones=["ngang", "nặng"]))
    out.append(concept("pron.there_is", "sentence", "pronouns", ["pronoun"], "There is a book", "Есть книга", "Có một cuốn sách",
                       en_ipa="/ðeə ɪz ə bʊk/", grammar=loc("There is + noun.", "Конструкция there is.", "There is."),
                       hint_en_ru="зе из э бук", hint_en_vi="the iz e buk", hint_ru_en="yest kniga", hint_ru_vi="yest kniga",
                       hint_vi_ru="ко мот куон шак", hint_vi_en="co mot cuon sach", tones=["sắc", "nột", "sắc", "sắc"]))
    out.append(concept("pron.there_are", "sentence", "pronouns", ["pronoun"], "There are two books", "Есть две книги", "Có hai cuốn sách",
                       grammar=loc("There are + plural.", "There are.", "There are."),
                       hint_en_ru="зе а ту букс", hint_en_vi="the a tu buks", hint_ru_en="yest dve knigi", hint_ru_vi="yest dve knigi",
                       hint_vi_ru="ко хай куон шак", hint_vi_en="co hai cuon sach", tones=["sắc", "ngang", "sắc", "sắc"]))
    return out


def auxiliaries() -> list[dict]:
    words = [
        ("aux.am", "am", "есть (я)", "là / thì (tôi)", ["auxiliary"]),
        ("aux.is", "is", "есть (он/она)", "là (anh ấy)", ["auxiliary"]),
        ("aux.are", "are", "есть (вы/мы/они)", "là (bạn/họ)", ["auxiliary"]),
        ("aux.was", "was", "был / была", "đã là (số ít)", ["auxiliary"]),
        ("aux.were", "were", "были", "đã là (số nhiều)", ["auxiliary"]),
        ("aux.be", "be", "быть", "là / thì / ở", ["auxiliary"]),
        ("aux.been", "been", "бывший (прич.)", "đã từng", ["auxiliary"]),
        ("aux.being", "being", "будучи", "đang là", ["auxiliary"]),
        ("aux.have", "have", "иметь / вспом. have", "có / trợ động từ have", ["auxiliary"]),
        ("aux.has", "has", "имеет", "có (ngôi 3)", ["auxiliary"]),
        ("aux.had", "had", "имел / had", "đã có", ["auxiliary"]),
        ("aux.do", "do", "делать / вспом. do", "làm / trợ do", ["auxiliary"]),
        ("aux.does", "does", "делает / does", "làm (ngôi 3)", ["auxiliary"]),
        ("aux.did", "did", "делал / did", "đã làm / did", ["auxiliary"]),
        ("aux.can", "can", "мочь", "có thể", ["auxiliary"]),
        ("aux.could", "could", "мог / мог бы", "có thể (quá khứ/lịch sự)", ["auxiliary"]),
        ("aux.may", "may", "можно / может быть", "có lẽ / được phép", ["auxiliary"]),
        ("aux.might", "might", "мог бы", "có lẽ", ["auxiliary"]),
        ("aux.must", "must", "должен", "phải", ["auxiliary"]),
        ("aux.shall", "shall", "shall (буду / предложу)", "sẽ (trang trọng)", ["auxiliary"]),
        ("aux.should", "should", "следует", "nên", ["auxiliary"]),
        ("aux.will", "will", "будет", "sẽ", ["auxiliary"]),
        ("aux.would", "would", "бы", "sẽ (giả định)", ["auxiliary"]),
        ("aux.ought_to", "ought to", "следует", "nên", ["auxiliary"]),
        ("aux.need_modal", "need", "нужно (модалка)", "cần", ["auxiliary"]),
        ("aux.used_to", "used to", "раньше (привычка)", "đã từng (thói quen)", ["auxiliary"]),
        ("aux.not", "not", "не", "không", ["auxiliary"]),
        ("aux.no", "no", "нет / никакой", "không / không có", ["auxiliary"]),
        ("aux.never", "never", "никогда", "không bao giờ", ["auxiliary"]),
        ("aux.dont", "don't", "не (я/вы/они)", "không (do not)", ["auxiliary"]),
        ("aux.doesnt", "doesn't", "не (он/она)", "không (does not)", ["auxiliary"]),
        ("aux.didnt", "didn't", "не (прош.)", "không (did not)", ["auxiliary"]),
        ("aux.isnt", "isn't", "не есть (он)", "không phải (is not)", ["auxiliary"]),
        ("aux.arent", "aren't", "не есть (вы)", "không phải (are not)", ["auxiliary"]),
        ("aux.wasnt", "wasn't", "не был", "đã không (was not)", ["auxiliary"]),
        ("aux.werent", "weren't", "не были", "đã không (were not)", ["auxiliary"]),
        ("aux.havent", "haven't", "не have", "chưa (have not)", ["auxiliary"]),
        ("aux.hasnt", "hasn't", "не has", "chưa (has not)", ["auxiliary"]),
        ("aux.cant", "can't", "не могу", "không thể", ["auxiliary"]),
        ("aux.wont", "won't", "не будет", "sẽ không", ["auxiliary"]),
    ]
    seen_en = {en.casefold() for _, en, _, _, _ in words}
    extra_theme = {}
    for en, ru, vi, theme in aux_extra():
        extra_theme[en.casefold()] = theme
        if en.casefold() in seen_en:
            continue
        seen_en.add(en.casefold())
        words.append((f"aux.{slug(en)}", en, ru, vi, ["auxiliary"]))
    out = []
    g = loc("Auxiliary / modal.", "Вспомогательный или модальный глагол.", "Trợ động từ / động từ khuyết thiếu.")
    be_do = {
        "am", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did",
    }
    for cid, en, ru, vi, tags in words:
        theme = extra_theme.get(en.casefold(), "be_do_have" if en.casefold() in be_do else "modals")
        kind = "phrase" if " " in en else "word"
        out.append(concept(cid, kind, theme, tags, en, ru, vi, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    skeletons = [
        ("aux.s.i_am", "I am a student", "Я студент", "Tôi là sinh viên", "be_do_have"),
        ("aux.s.do_you", "Do you like tea?", "Ты любишь чай?", "Bạn có thích trà không?", "be_do_have"),
        ("aux.s.i_dont", "I don't know", "Я не знаю", "Tôi không biết", "be_do_have"),
        ("aux.s.i_can", "I can help", "Я могу помочь", "Tôi có thể giúp", "modals"),
        ("aux.s.she_is", "She is here", "Она здесь", "Cô ấy ở đây", "be_do_have"),
        ("aux.s.they_are", "They are ready", "Они готовы", "Họ sẵn sàng", "be_do_have"),
        ("aux.s.he_has", "He has a car", "У него есть машина", "Anh ấy có xe", "be_do_have"),
        ("aux.s.i_have", "I have time", "У меня есть время", "Tôi có thời gian", "be_do_have"),
        ("aux.s.did_you", "Did you go?", "Ты ходил?", "Bạn đã đi chưa?", "be_do_have"),
        ("aux.s.i_will", "I will call you", "Я позвоню тебе", "Tôi sẽ gọi bạn", "modals"),
        ("aux.s.you_should", "You should rest", "Тебе следует отдохнуть", "Bạn nên nghỉ", "modals"),
        ("aux.s.i_must", "I must go", "Я должен идти", "Tôi phải đi", "modals"),
        ("aux.s.could_you", "Could you help me?", "Не могли бы вы помочь?", "Bạn có thể giúp tôi không?", "modals"),
        ("aux.s.i_used_to", "I used to live here", "Я раньше здесь жил", "Tôi đã từng sống ở đây", "modals"),
        ("aux.s.i_cant", "I can't swim", "Я не умею плавать", "Tôi không biết bơi", "modals"),
        ("aux.s.it_isnt", "It isn't far", "Это недалеко", "Không xa đâu", "be_do_have"),
        ("aux.s.i_havent", "I haven't eaten", "Я ещё не ел", "Tôi chưa ăn", "be_do_have"),
        ("aux.s.he_doesnt", "He doesn't work", "Он не работает", "Anh ấy không làm việc", "be_do_have"),
        ("aux.s.we_were", "We were tired", "Мы были усталыми", "Chúng tôi đã mệt", "be_do_have"),
        ("aux.s.she_was", "She was at home", "Она была дома", "Cô ấy đã ở nhà", "be_do_have"),
    ]
    sg = loc("Sentence frame with an auxiliary.", "Каркас со вспомогательным глаголом.", "Khung câu với trợ động từ.")
    for cid, en, ru, vi, theme in skeletons:
        out.append(concept(cid, "sentence", theme, ["auxiliary", "sentence"], en, ru, vi, grammar=sg,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return out


def questions() -> list[dict]:
    words = [
        ("q.what", "what", "что / какой", "gì / cái gì"),
        ("q.who", "who", "кто", "ai"),
        ("q.whom", "whom", "кого / кому", "ai (tân ngữ)"),
        ("q.whose", "whose", "чей", "của ai"),
        ("q.which", "which", "который / какой из", "cái nào"),
        ("q.where", "where", "где / куда", "ở đâu"),
        ("q.when", "when", "когда", "khi nào"),
        ("q.why", "why", "почему", "tại sao"),
        ("q.how", "how", "как", "như thế nào"),
        ("q.how_much", "how much", "сколько (неисчисл.)", "bao nhiêu"),
        ("q.how_many", "how many", "сколько (исчисл.)", "bao nhiêu (đếm được)"),
        ("q.how_long", "how long", "как долго", "bao lâu"),
        ("q.how_often", "how often", "как часто", "bao thường"),
        ("q.how_far", "how far", "как далеко", "bao xa"),
        ("q.how_old", "how old", "сколько лет", "bao nhiêu tuổi"),
        ("q.how_come", "how come", "как так / почему", "sao lại"),
        ("q.what_time", "what time", "во сколько", "mấy giờ"),
        ("q.what_kind_of", "what kind of", "какой вид", "loại gì"),
    ]
    out = []
    g = loc("Question word.", "Вопросительное слово.", "Từ để hỏi.")
    for cid, en, ru, vi in words:
        out.append(concept(cid, "word", "questions", ["question"], en, ru, vi, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    frames = [
        ("q.am_i", "Am I late?", "Я опаздываю?", "Tôi có muộn không?"),
        ("q.is_he", "Is he ready?", "Он готов?", "Anh ấy sẵn sàng chưa?"),
        ("q.are_you", "Are you OK?", "Ты в порядке?", "Bạn ổn chứ?"),
        ("q.was_it", "Was it good?", "Это было хорошо?", "Nó có tốt không?"),
        ("q.were_they", "Were they at home?", "Они были дома?", "Họ có ở nhà không?"),
        ("q.do_you", "Do you have water?", "У тебя есть вода?", "Bạn có nước không?"),
        ("q.does_she", "Does she work here?", "Она здесь работает?", "Cô ấy làm ở đây à?"),
        ("q.did_you", "Did you see that?", "Ты это видел?", "Bạn đã thấy chưa?"),
        ("q.can_i", "Can I sit here?", "Можно я здесь сяду?", "Tôi ngồi đây được không?"),
        ("q.could_you", "Could you wait?", "Вы не могли бы подождать?", "Bạn chờ được không?"),
        ("q.may_i", "May I come in?", "Можно войти?", "Tôi vào được không?"),
        ("q.should_i", "Should I call?", "Мне позвонить?", "Tôi có nên gọi không?"),
        ("q.would_you", "Would you like tea?", "Хотите чай?", "Bạn dùng trà chứ?"),
        ("q.will_you", "Will you help me?", "Ты мне поможешь?", "Bạn sẽ giúp tôi chứ?"),
        ("q.must_i", "Must I pay now?", "Я должен платить сейчас?", "Tôi phải trả ngay à?"),
        ("q.what_is", "What is this?", "Что это?", "Đây là gì?"),
        ("q.who_is", "Who is she?", "Кто она?", "Cô ấy là ai?"),
        ("q.where_is", "Where is the station?", "Где станция?", "Nhà ga ở đâu?"),
        ("q.when_is", "When is the train?", "Когда поезд?", "Tàu lúc nào?"),
        ("q.why_is", "Why is it closed?", "Почему закрыто?", "Sao lại đóng?"),
        ("q.how_is", "How is the food?", "Какая еда?", "Đồ ăn thế nào?"),
        ("q.whose_is", "Whose bag is this?", "Чья это сумка?", "Túi này của ai?"),
        ("q.which_is", "Which one is yours?", "Который твой?", "Cái nào là của bạn?"),
        ("q.what_do_you", "What do you want?", "Что ты хочешь?", "Bạn muốn gì?"),
        ("q.where_did", "Where did you go?", "Куда ты ходил?", "Bạn đã đi đâu?"),
        ("q.how_do_i", "How do I get there?", "Как туда добраться?", "Tôi đi tới đó thế nào?"),
        ("q.or_q", "Tea or coffee?", "Чай или кофе?", "Trà hay cà phê?"),
        ("q.whats_this", "What's this?", "Что это?", "Đây là gì?"),
        ("q.whats_that", "What's that?", "Что это там?", "Kia là gì?"),
        ("q.how_are_you", "How are you?", "Как дела?", "Bạn khỏe không?"),
        ("q.how_much_is", "How much is it?", "Сколько это стоит?", "Cái này bao nhiêu?"),
        ("q.where_is_the", "Where is the toilet?", "Где туалет?", "Nhà vệ sinh ở đâu?"),
        ("q.what_does_mean", "What does this mean?", "Что это значит?", "Cái này nghĩa là gì?"),
    ]
    fg = loc("Question frame.", "Схема вопроса.", "Khung câu hỏi.")
    for cid, en, ru, vi in frames:
        out.append(concept(cid, "sentence", "questions", ["question", "sentence"], en, ru, vi, grammar=fg,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return out


def function_words(skip: set[str] | None = None) -> list[dict]:
    rows = [
        ("fn.a", "a", "неопределённый артикль", "mạo từ a"),
        ("fn.an", "an", "артикль an", "mạo từ an"),
        ("fn.the", "the", "определённый артикль", "mạo từ the"),
        ("fn.and", "and", "и", "và"),
        ("fn.but", "but", "но", "nhưng"),
        ("fn.or", "or", "или", "hoặc"),
        ("fn.because", "because", "потому что", "bởi vì"),
        ("fn.so", "so", "поэтому / так", "vậy nên"),
        ("fn.if", "if", "если", "nếu"),
        ("fn.when_conj", "when", "когда (союз)", "khi"),
        ("fn.that_conj", "that", "что (союз)", "rằng"),
        ("fn.much", "much", "много (неисчисл.)", "nhiều"),
        ("fn.many", "many", "много (исчисл.)", "nhiều (đếm được)"),
        ("fn.a_lot_of", "a lot of", "много", "rất nhiều"),
        ("fn.a_few", "a few", "несколько", "một vài"),
        ("fn.a_little", "a little", "немного", "một chút"),
        ("fn.all", "all", "все", "tất cả"),
        ("fn.both", "both", "оба", "cả hai"),
        ("fn.each", "each", "каждый", "mỗi"),
        ("fn.every_q", "every", "каждый", "mỗi"),
        ("fn.please", "please", "пожалуйста", "làm ơn"),
        ("fn.thanks", "thanks", "спасибо", "cảm ơn"),
        ("fn.thank_you", "thank you", "спасибо", "cảm ơn bạn"),
        ("fn.sorry", "sorry", "извини", "xin lỗi"),
        ("fn.excuse_me", "excuse me", "простите / послушайте", "xin lỗi / làm ơn"),
        ("fn.youre_welcome", "you're welcome", "пожалуйста / не за что", "không có gì"),
    ]
    g = loc("Function word.", "Служебное слово.", "Từ chức năng.")
    out = []
    for cid, en, ru, vi in rows:
        out.append(concept(cid, "word" if " " not in en else "phrase", "function", ["function"], en, ru, vi, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    out.append(concept(
        "fn.zero_article_note", "phrase", "function", ["function"],
        "zero article (plural / abstract)", "нулевой артикль", "không dùng mạo từ",
        grammar=loc("No article before plural/abstract: Books are useful.", "Нулевой артикль.", "Không mạo từ."),
        hint_en_ru="зиро артикл", hint_en_vi="zero article", hint_ru_en="zero article", hint_ru_vi="zero article",
        hint_vi_ru="кхонг мао ты", hint_vi_en="khong mao tu",
    ))
    used = {en.casefold() for _, en, _, _ in rows}
    used.add("zero article (plural / abstract)")
    used |= set(skip or ())
    used_ids = {c["id"] for c in out}
    sections = [
        (lex_lines(CONJUNCTIONS), ["function", "conjunction"], loc("Conjunction.", "Союз.", "Liên từ."), "cj"),
        (lex_lines(DETERMINERS), ["function", "determiner"], loc("Determiner.", "Определитель.", "Từ hạn định."), "dt"),
        (lex_lines(PARTICLES), ["function", "particle"], loc("Particle or discourse marker.", "Частица.", "Tiểu từ."), "pt"),
    ]
    for section, tags, grammar, prefix in sections:
        for en, ru, vi in section:
            if en.casefold() in used:
                continue
            cid = f"{prefix}.{slug(en)}"
            if cid in used_ids:
                cid = f"{cid}_{len(used_ids)}"
            used.add(en.casefold())
            used_ids.add(cid)
            kind = "phrase" if " " in en else "word"
            out.append(concept(cid, kind, "function", tags, en, ru, vi, grammar=grammar,
                               hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return out


def preposition_concepts() -> list[dict]:
    lemma_g = loc("Preposition.", "Предлог.", "Giới từ.")
    phrase_g = loc("Preposition in a phrase.", "Предлог во фразе.", "Giới từ trong cụm.")
    out = []
    used: set[str] = set()
    used_ids: set[str] = set()
    for en, ru, vi in lex_lines(PREPOSITIONS):
        if en.casefold() in used:
            continue
        cid = prep_id(en)
        if cid in used_ids:
            cid = f"{cid}_{len(used_ids)}"
        used.add(en.casefold())
        used_ids.add(cid)
        kind = "phrase" if " " in en else "word"
        out.append(concept(cid, kind, "prepositions", ["function", "preposition"], en, ru, vi, grammar=lemma_g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    for en, ru, vi in lex_lines(PREP_PHRASES):
        if en.casefold() in used:
            continue
        cid = f"px.{slug(en)}"
        if cid in used_ids:
            cid = f"{cid}_{len(used_ids)}"
        used.add(en.casefold())
        used_ids.add(cid)
        out.append(concept(cid, "phrase", "prepositions", ["function", "preposition"], en, ru, vi, grammar=phrase_g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return out


def verbs() -> list[dict]:
    rows = [
        ("go", "идти / ехать", "đi"), ("come", "приходить", "đến"), ("leave", "уходить / оставлять", "rời / để lại"),
        ("stay", "оставаться", "ở lại"), ("live", "жить", "sống"), ("work", "работать", "làm việc"),
        ("study", "учиться", "học"), ("want", "хотеть", "muốn"), ("need", "нуждаться", "cần"),
        ("like", "нравиться", "thích"), ("love", "любить", "yêu"), ("know", "знать", "biết"),
        ("think", "думать", "nghĩ"), ("understand", "понимать", "hiểu"), ("remember", "помнить", "nhớ"),
        ("forget", "забывать", "quên"), ("see", "видеть", "thấy"), ("look", "смотреть / выглядеть", "nhìn"),
        ("watch", "смотреть (фильм)", "xem"), ("hear", "слышать", "nghe thấy"), ("listen", "слушать", "lắng nghe"),
        ("speak", "говорить", "nói"), ("say", "сказать", "nói (rằng)"), ("tell", "рассказать / велеть", "kể / bảo"),
        ("ask", "спрашивать / просить", "hỏi / xin"), ("answer", "отвечать", "trả lời"), ("call", "звонить / называть", "gọi"),
        ("read", "читать", "đọc"), ("write", "писать", "viết"), ("eat", "есть", "ăn"),
        ("drink", "пить", "uống"), ("cook", "готовить", "nấu"), ("buy", "покупать", "mua"),
        ("pay", "платить", "trả tiền"), ("sell", "продавать", "bán"), ("take", "брать", "lấy"),
        ("give", "давать", "cho"), ("get", "получать / добираться", "lấy / đến"), ("put", "класть", "đặt"),
        ("make", "делать / делать так", "làm / tạo"), ("use", "использовать", "dùng"), ("open", "открывать", "mở"),
        ("close", "закрывать", "đóng"), ("sit", "сидеть", "ngồi"), ("stand", "стоять", "đứng"),
        ("walk", "идти пешком", "đi bộ"), ("wait", "ждать", "chờ"), ("help", "помогать", "giúp"),
        ("start", "начинать", "bắt đầu"), ("stop", "останавливать(ся)", "dừng"), ("sleep", "спать", "ngủ"),
        ("wake", "просыпаться / будить", "thức dậy"), ("feel", "чувствовать", "cảm thấy"), ("try", "пытаться", "thử"),
        ("find", "находить", "tìm thấy"), ("lose", "терять", "mất"), ("bring", "приносить", "mang đến"),
        ("wear", "носить (одежду)", "mặc / đeo"), ("meet", "встречать", "gặp"), ("play", "играть", "chơi"),
        ("show", "показывать", "cho xem"), ("let", "позволять", "để / cho phép"), ("keep", "хранить / продолжать", "giữ"),
    ]
    g = loc("Verb. Use a phrase with I/you.", "Глагол. Учите во фразе.", "Động từ. Học trong câu.")
    out = []
    phrases = {
        "go": ("I go home", "Я иду домой", "Tôi về nhà"),
        "come": ("Come here", "Иди сюда", "Lại đây"),
        "want": ("I want coffee", "Я хочу кофе", "Tôi muốn cà phê"),
        "need": ("I need help", "Мне нужна помощь", "Tôi cần giúp"),
        "like": ("I like this", "Мне это нравится", "Tôi thích cái này"),
        "know": ("I know him", "Я его знаю", "Tôi biết anh ấy"),
        "eat": ("I eat rice", "Я ем рис", "Tôi ăn cơm"),
        "drink": ("I drink water", "Я пью воду", "Tôi uống nước"),
        "buy": ("I buy food", "Я покупаю еду", "Tôi mua đồ ăn"),
        "go_3": ("He goes to work", "Он ходит на работу", "Anh ấy đi làm"),
        "went": ("I went home", "Я пошёл домой", "Tôi đã về nhà"),
        "has_phr": ("She has a cat", "У неё есть кошка", "Cô ấy có một con mèo"),
    }
    for en, ru, vi in rows:
        cid = f"verb.{en}"
        out.append(concept(cid, "word", "verbs", ["verb"], en, ru, vi, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    extra = [
        ("verb.i_go_home", "I go home", "Я иду домой", "Tôi về nhà"),
        ("verb.he_goes", "He goes to work", "Он ходит на работу", "Anh ấy đi làm"),
        ("verb.i_went", "I went home", "Я пошёл домой", "Tôi đã về nhà"),
        ("verb.i_want_coffee", "I want coffee", "Я хочу кофе", "Tôi muốn cà phê"),
        ("verb.she_has", "She has a cat", "У неё есть кошка", "Cô ấy có một con mèo"),
        ("verb.i_need_help", "I need help", "Мне нужна помощь", "Tôi cần giúp đỡ"),
        ("verb.please_wait", "Please wait", "Подождите, пожалуйста", "Làm ơn chờ"),
        ("verb.lets_go", "Let's go", "Пойдём", "Đi thôi"),
    ]
    pg = loc("Common verb phrase.", "Частая глагольная фраза.", "Cụm động từ thường gặp.")
    for cid, en, ru, vi in extra:
        out.append(concept(cid, "sentence", "verbs", ["verb", "sentence"], en, ru, vi, grammar=pg,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return out


def adjectives() -> list[dict]:
    g = loc("Adjective. Often after be.", "Прилагательное. Часто после be.", "Tính từ. Thường sau be.")
    out: list[dict] = []
    data = [
        ("adj.good", "good", "хороший", "tốt"),
        ("adj.bad", "bad", "плохой", "xấu"),
        ("adj.big", "big", "большой", "to"),
        ("adj.small", "small", "маленький", "nhỏ"),
        ("adj.new", "new", "новый", "mới"),
        ("adj.old", "old", "старый", "cũ"),
        ("adj.young", "young", "молодой", "trẻ"),
        ("adj.hot", "hot", "горячий", "nóng"),
        ("adj.cold", "cold", "холодный", "lạnh"),
        ("adj.warm", "warm", "тёплый", "ấm"),
        ("adj.cool", "cool", "прохладный", "mát"),
        ("adj.cheap", "cheap", "дешёвый", "rẻ"),
        ("adj.expensive", "expensive", "дорогой", "đắt"),
        ("adj.easy", "easy", "лёгкий", "dễ"),
        ("adj.hard", "hard", "трудный", "khó"),
        ("adj.fast", "fast", "быстрый", "nhanh"),
        ("adj.slow", "slow", "медленный", "chậm"),
        ("adj.near", "near", "близкий", "gần"),
        ("adj.far", "far", "далёкий", "xa"),
        ("adj.open", "open", "открытый", "mở"),
        ("adj.closed", "closed", "закрытый", "đóng"),
        ("adj.hungry", "hungry", "голодный", "đói"),
        ("adj.thirsty", "thirsty", "хочет пить", "khát"),
        ("adj.tired", "tired", "уставший", "mệt"),
        ("adj.happy", "happy", "счастливый", "vui"),
        ("adj.sad", "sad", "грустный", "buồn"),
        ("adj.beautiful", "beautiful", "красивый", "đẹp"),
        ("adj.delicious", "delicious", "вкусный", "ngon"),
        ("adj.right", "right", "правильный", "đúng"),
        ("adj.wrong", "wrong", "неправильный", "sai"),
        ("adj.clean", "clean", "чистый", "sạch"),
        ("adj.dirty", "dirty", "грязный", "bẩn"),
        ("adj.full", "full", "полный", "đầy"),
        ("adj.empty", "empty", "пустой", "rỗng"),
        ("adj.early", "early", "ранний", "sớm"),
        ("adj.late", "late", "поздний", "muộn"),
        ("adj.busy", "busy", "занятый", "bận"),
        ("adj.ready", "ready", "готовый", "sẵn sàng"),
        ("adj.important", "important", "важный", "quan trọng"),
        ("adj.same", "same", "такой же", "giống"),
        ("adj.different", "different", "разный", "khác"),
        ("adj.next", "next", "следующий", "tiếp theo"),
        ("adj.last", "last", "последний", "cuối"),
    ]
    for cid, en, ru, vi in data:
        out.append(concept(cid, "word", "adjectives", ["adjective"], en, ru, vi, grammar=g,
                           hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    out.append(concept("adj.it_is_good", "sentence", "adjectives", ["adjective", "sentence"],
                       "It is good", "Это хорошо", "Nó tốt", grammar=g,
                       hint_en_ru="ит из гуд", hint_en_vi="it iz gud", hint_ru_en="eto khorosho", hint_ru_vi="eto khorosho",
                       hint_vi_ru="но тот", hint_vi_en="no tot"))
    return out


def theme_pack(theme_id: str, order: int, title: dict, rows: list[tuple], grammar: dict | None = None) -> dict:
    fallback = grammar or loc("Everyday phrase.", "Разговорная фраза.", "Câu giao tiếp.")
    phrase_g = loc("Phrase.", "Фраза.", "Cụm từ.")
    concepts = []
    for cid, kind, en, ru, vi in rows:
        g = phrase_g if grammar and grammar.get("en") == "Word." and kind != "word" else fallback
        concepts.append(concept(cid, kind, theme_id, [f"theme:{theme_id}"], en, ru, vi, grammar=g,
                                hint_en_ru=en, hint_en_vi=en, hint_ru_en=ru, hint_ru_vi=ru, hint_vi_ru=vi, hint_vi_en=vi))
    return pack(theme_id, order, title, concepts)


def load_primers() -> list[dict]:
    """Language lessons live in content-src/primers and are copied, not shortened."""
    folder = SRC / "primers"
    names = ("vi.json", "ru.json", "en.json")
    themes = []
    for name in names:
        path = folder / name
        theme = json.loads(path.read_text(encoding="utf-8"))
        if theme.get("kind") != "primer":
            raise SystemExit(f"{path} must be kind primer")
        if not theme.get("sections"):
            raise SystemExit(f"{path} has no sections")
        themes.append(theme)
    return themes


PHRASE_PACK = "96_phrases.json"


def write_composed() -> None:
    """Build phrase lessons whose uses point at vocabulary concept ids."""
    recipes_path = SRC / "phrases" / "recipes.json"
    recipes = json.loads(recipes_path.read_text(encoding="utf-8"))
    vocab: set[str] = set()
    for path in sorted(PACKS.glob("*.json")):
        if path.name == PHRASE_PACK:
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        for row in data.get("concepts", []):
            vocab.add(row["id"])
    if not isinstance(recipes, list) or len(recipes) != 36:
        raise SystemExit(f"{recipes_path} must contain 36 recipes")
    recipe_ids = {row["id"] for row in recipes}
    seen: set[str] = set()
    by_level = {1: 0, 2: 0, 3: 0}
    concepts = []
    for row in recipes:
        cid = row["id"]
        level = row["level"]
        kind = row["kind"]
        uses = row["uses"]
        if cid in seen:
            raise SystemExit(f"Duplicate phrase id {cid}")
        seen.add(cid)
        if cid in vocab or cid in uses:
            raise SystemExit(f"{cid} collides with a word or uses itself")
        if not isinstance(uses, list) or len(uses) != len(set(uses)):
            raise SystemExit(f"{cid} uses must be unique")
        missing = [word for word in uses if word not in vocab]
        if missing:
            raise SystemExit(f"{cid} missing words: {missing}")
        if any(word in recipe_ids for word in uses):
            raise SystemExit(f"{cid} uses another phrase")
        if level == 1:
            if kind != "phrase" or len(uses) != 2:
                raise SystemExit(f"{cid} level 1 needs kind phrase and 2 words")
        elif level == 2:
            if kind != "sentence" or len(uses) != 3:
                raise SystemExit(f"{cid} level 2 needs kind sentence and 3 words")
        elif level == 3:
            if kind != "sentence" or len(uses) < 4:
                raise SystemExit(f"{cid} level 3 needs kind sentence and at least 4 words")
        else:
            raise SystemExit(f"{cid} level must be 1, 2, or 3")
        by_level[level] += 1
        en, ru, vi = row["en"], row["ru"], row["vi"]
        for lang, text in (("en", en), ("ru", ru), ("vi", vi)):
            if len(text.split()) < 2:
                raise SystemExit(f"{cid} {lang} needs at least two words")
        grammar = row["grammar"]
        concepts.append(
            concept(
                cid,
                kind,
                "composed",
                ["composed", f"level:{level}"],
                en,
                ru,
                vi,
                grammar=loc(grammar["en"], grammar["ru"], grammar["vi"]),
                level=level,
                uses=uses,
            )
        )
    if by_level != {1: 12, 2: 12, 3: 12}:
        raise SystemExit(f"Need 12 recipes per level, got {by_level}")
    write_pack(
        PHRASE_PACK,
        pack(
            "composed",
            196,
            loc("Phrases", "Фразы", "Cụm từ"),
            concepts,
            kind="composed",
            description=loc(
                "Sentences from words you already answered.",
                "Предложения из слов, на которые вы уже ответили.",
                "Câu từ những từ bạn đã trả lời.",
            ),
        ),
    )


def main() -> None:
    write_pack("00_primers.json", {"themes": load_primers(), "concepts": []})

    p = pronouns()
    write_pack("10_pronouns.json", pack("pronouns", 10, loc("Pronouns", "Местоимения", "Đại từ"), p))
    aux = auxiliaries()
    be = [c for c in aux if c["theme"] == "be_do_have"]
    mod = [c for c in aux if c["theme"] == "modals"]
    write_pack("20_be_do_have.json", pack("be_do_have", 20, loc("Be, do, have", "Be, do, have", "Be, do, have"), be))
    write_pack("30_modals.json", pack("modals", 30, loc("Modals", "Модалки", "Động từ khuyết thiếu"), mod))
    qs = questions()
    write_pack("40_questions.json", pack("questions", 40, loc("Questions", "Вопросы", "Câu hỏi"), qs))
    preps = preposition_concepts()
    write_pack(
        "48_prepositions.json",
        pack(
            "prepositions",
            48,
            loc("Prepositions", "Предлоги", "Giới từ"),
            preps,
            description=loc(
                "Prepositions and the phrases they build.",
                "Предлоги и фразы с ними.",
                "Giới từ và cụm giới từ.",
            ),
        ),
    )
    prep_en = {c["texts"]["en"]["text"].casefold() for c in preps}
    fn = function_words(prep_en)
    write_pack("50_function.json", pack("function", 50, loc("Function words", "Служебные слова", "Từ chức năng"), fn))
    write_pack("60_verbs.json", pack("verbs", 60, loc("Verbs", "Глаголы", "Động từ"), verbs()))
    write_pack("70_adjectives.json", pack("adjectives", 70, loc("Adjectives", "Прилагательные", "Tính từ"), adjectives()))

    write_pack("80_greetings.json", theme_pack("greetings", 80, loc("Greetings", "Приветствия", "Chào hỏi"), [
        ("gr.hello", "word", "hello", "привет", "xin chào"),
        ("gr.hi", "word", "hi", "привет", "chào"),
        ("gr.good_morning", "phrase", "good morning", "доброе утро", "chào buổi sáng"),
        ("gr.good_afternoon", "phrase", "good afternoon", "добрый день", "chào buổi chiều"),
        ("gr.good_evening", "phrase", "good evening", "добрый вечер", "chào buổi tối"),
        ("gr.goodbye", "word", "goodbye", "до свидания", "tạm biệt"),
        ("gr.bye", "word", "bye", "пока", "bye"),
        ("gr.see_you", "phrase", "see you", "увидимся", "hẹn gặp lại"),
        ("gr.nice_to_meet_you", "sentence", "Nice to meet you", "Приятно познакомиться", "Rất vui được gặp bạn"),
        ("gr.my_name_is", "sentence", "My name is Anna", "Меня зовут Анна", "Tên tôi là Anna"),
        ("gr.whats_your_name", "sentence", "What's your name?", "Как тебя зовут?", "Bạn tên gì?"),
        ("gr.im_fine", "sentence", "I'm fine, thanks", "Я в порядке, спасибо", "Tôi khỏe, cảm ơn"),
        ("gr.yes", "word", "yes", "да", "vâng / có"),
        ("gr.no", "word", "no", "нет", "không"),
        ("gr.ok", "word", "OK", "хорошо", "được"),
    ]))
    write_pack("81_numbers.json", theme_pack("numbers", 81, loc("Numbers", "Числа", "Số"), [
        ("num.0", "word", "zero", "ноль", "không"),
        ("num.1", "word", "one", "один", "một"),
        ("num.2", "word", "two", "два", "hai"),
        ("num.3", "word", "three", "три", "ba"),
        ("num.4", "word", "four", "четыре", "bốn"),
        ("num.5", "word", "five", "пять", "năm"),
        ("num.6", "word", "six", "шесть", "sáu"),
        ("num.7", "word", "seven", "семь", "bảy"),
        ("num.8", "word", "eight", "восемь", "tám"),
        ("num.9", "word", "nine", "девять", "chín"),
        ("num.10", "word", "ten", "десять", "mười"),
        ("num.100", "word", "one hundred", "сто", "một trăm"),
        ("num.how_many_people", "sentence", "How many people?", "Сколько человек?", "Bao nhiêu người?"),
        ("num.first", "word", "first", "первый", "thứ nhất"),
        ("num.second", "word", "second", "второй", "thứ hai"),
    ]))
    write_pack("82_food.json", theme_pack("food", 82, loc("Food", "Еда", "Đồ ăn"), [
        ("food.water", "word", "water", "вода", "nước"),
        ("food.rice", "word", "rice", "рис", "cơm / gạo"),
        ("food.bread", "word", "bread", "хлеб", "bánh mì"),
        ("food.coffee", "word", "coffee", "кофе", "cà phê"),
        ("food.tea", "word", "tea", "чай", "trà"),
        ("food.milk", "word", "milk", "молоко", "sữa"),
        ("food.meat", "word", "meat", "мясо", "thịt"),
        ("food.fish", "word", "fish", "рыба", "cá"),
        ("food.fruit", "word", "fruit", "фрукты", "trái cây"),
        ("food.vegetable", "word", "vegetable", "овощ", "rau"),
        ("food.im_hungry", "sentence", "I'm hungry", "Я голоден", "Tôi đói"),
        ("food.im_thirsty", "sentence", "I'm thirsty", "Я хочу пить", "Tôi khát"),
        ("food.this_is_delicious", "sentence", "This is delicious", "Это вкусно", "Cái này ngon"),
        ("food.no_spicy", "sentence", "Not spicy, please", "Пожалуйста, не острое", "Làm ơn đừng cay"),
        ("food.the_bill", "sentence", "The bill, please", "Счёт, пожалуйста", "Tính tiền giúp tôi"),
    ]))
    write_pack("83_cafe.json", theme_pack("cafe", 83, loc("Cafe", "Кафе", "Quán cà phê"), [
        ("cafe.i_want_coffee", "sentence", "I want coffee", "Я хочу кофе", "Tôi muốn cà phê"),
        ("cafe.can_i_have", "sentence", "Can I have a menu?", "Можно меню?", "Cho tôi xem thực đơn?"),
        ("cafe.for_here", "phrase", "for here", "здесь", "dùng tại chỗ"),
        ("cafe.to_go", "phrase", "to go", "с собой", "mang đi"),
        ("cafe.table_for_two", "sentence", "A table for two", "Столик на двоих", "Bàn cho hai người"),
        ("cafe.water_please", "sentence", "Water, please", "Воды, пожалуйста", "Cho tôi nước"),
        ("cafe.its_too_hot", "sentence", "It's too hot", "Слишком горячо", "Nóng quá"),
        ("cafe.sugar", "word", "sugar", "сахар", "đường"),
        ("cafe.cup", "word", "cup", "чашка", "tách / ly"),
        ("cafe.waiter", "word", "waiter", "официант", "nhân viên phục vụ"),
        ("cafe.i_would_like", "sentence", "I would like tea", "Я бы хотел чай", "Tôi muốn trà"),
        ("cafe.how_much_coffee", "sentence", "How much is the coffee?", "Сколько стоит кофе?", "Cà phê bao nhiêu?"),
    ]))
    write_pack("84_transport.json", theme_pack("transport", 84, loc("Transport", "Транспорт", "Đi lại"), [
        ("tr.bus", "word", "bus", "автобус", "xe buýt"),
        ("tr.train", "word", "train", "поезд", "tàu"),
        ("tr.taxi", "word", "taxi", "такси", "taxi"),
        ("tr.airport", "word", "airport", "аэропорт", "sân bay"),
        ("tr.ticket", "word", "ticket", "билет", "vé"),
        ("tr.where_is_bus", "sentence", "Where is the bus stop?", "Где остановка?", "Trạm xe buýt ở đâu?"),
        ("tr.i_need_a_ticket", "sentence", "I need a ticket", "Мне нужен билет", "Tôi cần một vé"),
        ("tr.how_do_i_get", "sentence", "How do I get to the hotel?", "Как доехать до отеля?", "Đến khách sạn đi thế nào?"),
        ("tr.left", "word", "left", "налево", "trái"),
        ("tr.right", "word", "right", "направо", "phải"),
        ("tr.straight", "phrase", "go straight", "прямо", "đi thẳng"),
        ("tr.stop_here", "sentence", "Stop here, please", "Остановите здесь, пожалуйста", "Dừng đây giúp tôi"),
    ]))
    write_pack("85_shopping.json", theme_pack("shopping", 85, loc("Shopping", "Покупки", "Mua sắm"), [
        ("sh.how_much", "sentence", "How much is this?", "Сколько это стоит?", "Cái này bao nhiêu?"),
        ("sh.too_expensive", "sentence", "It's too expensive", "Это слишком дорого", "Đắt quá"),
        ("sh.cheaper", "sentence", "Do you have a cheaper one?", "Есть дешевле?", "Có cái rẻ hơn không?"),
        ("sh.i_take_it", "sentence", "I'll take it", "Я беру", "Tôi lấy cái này"),
        ("sh.size", "word", "size", "размер", "cỡ"),
        ("sh.color", "word", "color", "цвет", "màu"),
        ("sh.cash", "word", "cash", "наличные", "tiền mặt"),
        ("sh.card", "word", "card", "карта", "thẻ"),
        ("sh.receipt", "word", "receipt", "чек", "hóa đơn"),
        ("sh.do_you_have", "sentence", "Do you have this in blue?", "Это есть синим?", "Có màu xanh không?"),
        ("sh.where_is_market", "sentence", "Where is the market?", "Где рынок?", "Chợ ở đâu?"),
        ("sh.i_am_just_looking", "sentence", "I'm just looking", "Я просто смотрю", "Tôi chỉ xem thôi"),
    ]))
    write_pack("86_time.json", theme_pack("time", 86, loc("Time", "Время", "Thời gian"), [
        ("time.today", "word", "today", "сегодня", "hôm nay"),
        ("time.tomorrow", "word", "tomorrow", "завтра", "ngày mai"),
        ("time.yesterday", "word", "yesterday", "вчера", "hôm qua"),
        ("time.now", "word", "now", "сейчас", "bây giờ"),
        ("time.later", "word", "later", "позже", "sau"),
        ("time.morning", "word", "morning", "утро", "buổi sáng"),
        ("time.evening", "word", "evening", "вечер", "buổi tối"),
        ("time.what_time", "sentence", "What time is it?", "Который час?", "Mấy giờ rồi?"),
        ("time.monday", "word", "Monday", "понедельник", "thứ hai"),
        ("time.week", "word", "week", "неделя", "tuần"),
        ("time.month", "word", "month", "месяц", "tháng"),
        ("time.year", "word", "year", "год", "năm"),
        ("time.see_you_tomorrow", "sentence", "See you tomorrow", "До завтра", "Hẹn gặp ngày mai"),
        ("time.im_late", "sentence", "I'm late", "Я опаздываю", "Tôi muộn"),
    ]))
    write_pack("87_polite.json", theme_pack("polite", 87, loc("Polite phrases", "Вежливые фразы", "Câu lịch sự"), [
        ("pol.please_help", "sentence", "Please help me", "Помогите, пожалуйста", "Làm ơn giúp tôi"),
        ("pol.i_dont_understand", "sentence", "I don't understand", "Я не понимаю", "Tôi không hiểu"),
        ("pol.slowly", "sentence", "Slowly, please", "Помедленнее, пожалуйста", "Nói chậm giúp tôi"),
        ("pol.repeat", "sentence", "Can you repeat that?", "Повторите, пожалуйста", "Bạn nhắc lại được không?"),
        ("pol.do_you_speak_en", "sentence", "Do you speak English?", "Вы говорите по-английски?", "Bạn nói tiếng Anh không?"),
        ("pol.i_speak_a_little", "sentence", "I speak a little", "Я говорю немного", "Tôi nói một chút"),
        ("pol.congratulations", "word", "congratulations", "поздравляю", "chúc mừng"),
        ("pol.good_luck", "phrase", "good luck", "удачи", "chúc may mắn"),
        ("pol.take_care", "phrase", "take care", "берегите себя", "bảo trọng"),
        ("pol.after_you", "phrase", "after you", "после вас", "mời bạn trước"),
        ("pol.bless_you", "phrase", "bless you", "будь здоров", "chúc sức khỏe"),
        ("pol.welcome", "word", "welcome", "добро пожаловать", "chào mừng"),
        ("pol.no_problem", "phrase", "no problem", "нет проблем", "không sao"),
        ("pol.of_course", "phrase", "of course", "конечно", "dĩ nhiên"),
        ("pol.maybe", "word", "maybe", "может быть", "có lẽ"),
    ]))
    seen_pairs: set[tuple[str, str]] = set()
    seen_ids: set[str] = set()
    for f in sorted(PACKS.glob("*.json")):
        if f.name[:2] >= "88" and f.name[:2] <= "97":
            continue
        if f.name.startswith("98"):
            continue
        data = json.loads(f.read_text(encoding="utf-8"))
        for c in data.get("concepts", []):
            seen_ids.add(c["id"])
            en = c["texts"]["en"]["text"].casefold()
            ru = c["texts"]["ru"]["text"].casefold()
            seen_pairs.add((en, ru))

    for filename, theme_id, order, title, rows in extra_specs():
        kept = []
        for cid, kind, en, ru, vi in rows:
            key = (en.casefold(), ru.casefold())
            if key in seen_pairs or cid in seen_ids:
                continue
            seen_pairs.add(key)
            seen_ids.add(cid)
            kept.append((cid, kind, en, ru, vi))
        if kept:
            write_pack(filename, theme_pack(theme_id, order, title, kept))

    pending = []
    for en, ru, vi in learner_rows():
        key = (en.casefold(), ru.casefold())
        cid = f"lex.{slug(en)}"
        if key in seen_pairs or cid in seen_ids:
            cid = f"lex.{slug(en)}_{len(seen_ids)}"
            if cid in seen_ids:
                continue
        if key in seen_pairs:
            continue
        seen_pairs.add(key)
        seen_ids.add(cid)
        kind = "phrase" if " " in en else "word"
        pending.append((cid, kind, en, ru, vi))
    band = 0
    while pending:
        chunk = pending[:2000]
        pending = pending[2000:]
        band += 1
        letter = chr(ord("a") + band - 1)
        write_pack(
            f"98{letter}_core.json",
            theme_pack(
                f"core_{letter}",
                110 + band,
                loc(f"Core words {band}", f"Базовые слова {band}", f"Từ vựng {band}"),
                chunk,
                grammar=loc("Word.", "Слово.", "Từ."),
            ),
        )

    write_pack("99_user.json", pack("user", 200, loc("My cards", "Мои карточки", "Thẻ của tôi"), []))
    write_composed()

    ids = []
    for f in sorted(PACKS.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        for c in data.get("concepts", []):
            ids.append(c["id"])
    required = [
        "pron.i", "pron.you", "pron.he", "pron.she", "pron.it", "pron.we", "pron.they",
        "pron.me", "pron.him", "pron.her_obj", "pron.us", "pron.them",
        "pron.my", "pron.your", "pron.his_adj", "pron.her_adj", "pron.its", "pron.our", "pron.their",
        "pron.mine", "pron.yours", "pron.hers", "pron.ours", "pron.theirs",
        "pron.myself", "pron.yourself", "pron.himself", "pron.herself", "pron.itself",
        "pron.ourselves", "pron.yourselves", "pron.themselves",
        "pron.this", "pron.that", "pron.these", "pron.those",
        "pron.some", "pron.any", "pron.no_det", "pron.every",
        "pron.someone", "pron.anyone", "pron.everyone", "pron.no_one",
        "pron.something", "pron.anything", "pron.nothing", "pron.everything",
        "pron.each_other", "pron.rel_who", "pron.rel_which", "pron.rel_that", "pron.rel_whose",
        "pron.dummy_it", "pron.there_is", "pron.there_are",
        "aux.am", "aux.is", "aux.are", "aux.was", "aux.were", "aux.be", "aux.been", "aux.being",
        "aux.have", "aux.has", "aux.had", "aux.do", "aux.does", "aux.did",
        "aux.can", "aux.could", "aux.may", "aux.might", "aux.must", "aux.shall", "aux.should",
        "aux.will", "aux.would", "aux.ought_to", "aux.need_modal", "aux.used_to",
        "aux.not", "aux.dont", "aux.doesnt", "aux.didnt", "aux.isnt", "aux.arent",
        "aux.wasnt", "aux.werent", "aux.havent", "aux.hasnt", "aux.cant", "aux.wont",
        "q.what", "q.who", "q.whom", "q.whose", "q.which", "q.where", "q.when", "q.why", "q.how",
        "q.how_much", "q.how_many", "q.how_long", "q.how_often", "q.how_far", "q.how_old", "q.how_come",
        "q.what_time", "q.what_kind_of",
        "q.am_i", "q.do_you", "q.can_i", "q.what_is", "q.how_are_you", "q.how_much_is", "q.what_does_mean",
        "fn.a", "fn.an", "fn.the", "fn.in", "fn.on", "fn.at", "fn.to", "fn.from", "fn.with", "fn.for", "fn.of",
        "fn.please", "fn.thank_you", "fn.sorry", "fn.excuse_me", "fn.youre_welcome",
    ]
    for en, _, _ in lex_lines(PREPOSITIONS):
        pid = prep_id(en)
        if pid not in required:
            required.append(pid)
    for extra_id in (
        "aux.have_to", "aux.mustn_t", "aux.shouldn_t", "aux.couldn_t", "aux.wouldn_t",
        "aux.going_to", "aux.be_able_to", "aux.let_s", "aux.had_better", "aux.would_rather",
    ):
        if extra_id not in required:
            required.append(extra_id)
    checklist = SRC / "en-closed-class.md"
    lines = ["# English closed class checklist\n", "Every id below must exist in generated JSON with EN+RU+VI.\n"]
    for i in required:
        lines.append(f"- {i}\n")
    checklist.write_text("".join(lines), encoding="utf-8")
    missing = [i for i in required if i not in ids]
    if missing:
        raise SystemExit(f"Missing ids: {missing}")
    fake = []
    empty = []
    for f in sorted(PACKS.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        for c in data.get("concepts", []):
            for lang, form in c["texts"].items():
                ipa = form.get("ipa", "")
                text = form.get("text", "")
                if not ipa.strip("/").strip():
                    empty.append(f"{c['id']}.{lang}")
                elif lang == "en" and looks_like_orthography(text, ipa):
                    fake.append(f"{c['id']}.{lang}:{text}->{ipa}")
    if empty:
        raise SystemExit(f"Empty IPA: {empty[:20]}")
    if fake:
        raise SystemExit(f"Orthography IPA: {fake[:20]}")
    print(f"Wrote {len(ids)} concepts, checklist {len(required)}")
    uniques = {lang: set() for lang in ("en", "ru", "vi")}
    for f in sorted(PACKS.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        for c in data.get("concepts", []):
            for lang, form in c["texts"].items():
                uniques[lang].add(form["text"].casefold())
    counts = {k: len(v) for k, v in uniques.items()}
    print(f"Unique lemmas {counts}")
    if len(ids) < 6000:
        raise SystemExit(f"Need >=6000 concepts, got {len(ids)}")
    short = {k: n for k, n in counts.items() if n < 5000}
    if short:
        raise SystemExit(f"Need >=5000 unique forms per language, got {short}")


if __name__ == "__main__":
    main()
