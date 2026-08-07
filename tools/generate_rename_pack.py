"""Regenerate the rename overlay pack (resourcepacks/primordial_compat) from Alex's Caves' jar.

The mod's identity is that Alex's Caves' Primordial Caves creatures carry vanilla-style names:

    Grottoceratops -> Grazer        Relicheirus  -> Logger      Tremorsaurus -> Roarer
    Subterranodon  -> Drifter       Vallumraptor -> Stealer     Tremorzilla  -> Roarerzilla
    Atlatitan      -> Rammer        Luxtructosaurus -> Scorcher

This script derives, for every locale Alex's Caves ships, everything that names them:
  * the entity names themselves (proper nouns of this mod, kept untranslated),
  * every other lang key whose LOCALIZED value mentions a creature (egg blocks, spawn eggs,
    sound subtitles, cave paintings, advancement titles/descriptions, the Extinction Spear
    enchantment descriptions, ...), with the localized name substituted IN PLACE so each
    locale's own grammar and word order survive ("Huevo de Roarer", "Rammer Yumurtasi"),
  * the guide-book pages (books/<locale>/**.txt) whose prose mentions a creature.

The output is a MINIMAL overlay: only the keys/pages that actually change are written, so the
pack layers our names over Alex's Caves' living translations instead of freezing a copy of them.
The pack is registered above every mod resource pack (see CompatLangPack) and the whole thing is
gated by the rename_mobs config option.

Run:  python3 tools/generate_rename_pack.py     (from the primordialmobs project dir)
"""
import json
import os
import re
import shutil
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
PACK = os.path.join(PROJ, 'src/main/resources/resourcepacks/primordial_compat/assets/alexscaves')
AC_JAR = os.path.join(PROJ, 'libs/alexscaves-full-2.0.2.jar')

# Alex's Caves entity id -> the Primordial Mobs name. Order matters only through the
# longest-localized-name-first sort below (zh_cn 撼地龙 / 撼地斯拉 nest, for example).
MOBS = {
    'grottoceratops': 'Grazer',
    'relicheirus': 'Logger',
    'tremorsaurus': 'Roarer',
    'subterranodon': 'Drifter',
    'vallumraptor': 'Stealer',
    'tremorzilla': 'Roarerzilla',
    'atlatitan': 'Rammer',
    'luxtructosaurus': 'Scorcher',
}

# Alex's Caves strings whose creature name is misspelled in that locale, so substring
# substitution cannot find it. Verified against alexscaves-full-2.0.2.
MANUAL = {
    ('pl_pl', 'block.alexscaves.vallumraptor_egg'): 'Jajo Stealera',
    ('zh_tw', 'item.alexscaves.spawn_egg_subterranodon'): 'Drifter 生怪蛋',
}

# Locales that decline the creature name inside a sentence. Exact-substring matching misses
# those ("Яйцо сотрясозиллы", "Jajo tremorzilli"), so the name is also matched on its stem
# (minus the final letter) and whatever lowercase ending follows is swallowed: a Cyrillic or
# Polish case ending glued to a Latin proper noun reads wrong either way.
INFLECTED = {'ru_ru', 'uk_ua', 'pl_pl'}
INFLECTION_TAIL = re.compile(r'^[a-zа-яёіїєґąćęłńóśźż́]+')
MIN_STEM = 5


def match_forms(locale, ac_name):
    forms = [ac_name]
    if locale in INFLECTED and len(ac_name) - 1 >= MIN_STEM:
        forms.append(ac_name[:-1])
    return forms


def substitute_all(locale, text, pairs):
    """Replace every occurrence of each localized creature name in `text` with the mod name."""
    out = text
    for ac_name, english in pairs:
        for form in match_forms(locale, ac_name):
            i = 0
            while True:
                j = out.lower().find(form.lower(), i)
                if j < 0:
                    break
                head, tail = out[:j], out[j + len(form):]
                if locale in INFLECTED:
                    tail = INFLECTION_TAIL.sub('', tail)
                out = head + english + tail
                i = j + len(english)
    return out


# English article agreement: "an Atlatitan" -> "an Rammer" reads wrong, fix to "a Rammer".
# Only names that start with a consonant sound need it; applied to en_us text only.
EN_ARTICLE = re.compile(r'\b(a|an|A|An) (Rammer|Roarer|Roarerzilla|Logger|Drifter|Stealer|Grazer|Scorcher)\b')


def fix_english_articles(text):
    return EN_ARTICLE.sub(lambda m: ('a' if m.group(1).islower() else 'A') + ' ' + m.group(2), text)


BOOK_LINK = re.compile(r'\{([^{}|]+)\|([^{}]+)\}')


def substitute_book(locale, text, pairs):
    """Rename visible book prose and link labels without changing resource-path link targets."""
    out = []
    cursor = 0
    for match in BOOK_LINK.finditer(text):
        out.append(substitute_all(locale, text[cursor:match.start()], pairs))
        label = substitute_all(locale, match.group(1), pairs)
        out.append('{%s|%s}' % (label, match.group(2)))
        cursor = match.end()
    out.append(substitute_all(locale, text[cursor:], pairs))
    return ''.join(out)


def name_pairs(ac_lang, ac_en):
    """(localized Alex's Caves name, our name) pairs, longest first, deduplicated."""
    pairs = []
    for mob, english in MOBS.items():
        key = 'entity.alexscaves.%s' % mob
        for candidate in (ac_lang.get(key), ac_en.get(key)):
            if candidate and (candidate, english) not in pairs:
                pairs.append((candidate, english))
    pairs.sort(key=lambda p: -len(p[0]))
    return pairs


def main():
    jar = zipfile.ZipFile(AC_JAR)
    ac_en = json.loads(jar.read('assets/alexscaves/lang/en_us.json'))
    locales = sorted(n.rsplit('/', 1)[1][:-5] for n in jar.namelist()
                     if n.startswith('assets/alexscaves/lang/') and n.endswith('.json'))

    # Regenerate from scratch: stale pages/keys from earlier runs must not survive.
    for sub in ('lang', 'books'):
        path = os.path.join(PACK, sub)
        if os.path.isdir(path):
            shutil.rmtree(path)
    os.makedirs(os.path.join(PACK, 'lang'))

    # ---- lang overlays ----------------------------------------------------------------------
    total = 0
    for locale in locales:
        ac = json.loads(jar.read('assets/alexscaves/lang/%s.json' % locale))
        pairs = name_pairs(ac, ac_en)
        out = {}
        for key, ac_value in ac.items():
            if not isinstance(ac_value, str):
                continue
            if (locale, key) in MANUAL:
                out[key] = MANUAL[(locale, key)]
                continue
            new = substitute_all(locale, ac_value, pairs)
            if locale == 'en_us':
                new = fix_english_articles(new)
            if new != ac_value:
                out[key] = new
        # The renamed creatures' own names are proper nouns of this mod, untranslated.
        for mob, english in MOBS.items():
            out['entity.alexscaves.%s' % mob] = english
        # Safety net: the derived name of every egg block / spawn egg must actually have been
        # renamed; a locale that slipped through falls back to the English pattern.
        for mob, english in MOBS.items():
            for key in ('block.alexscaves.%s_egg' % mob, 'item.alexscaves.spawn_egg_%s' % mob):
                if key in ac and key not in out:
                    fallback = substitute_all('en_us', ac_en[key], name_pairs(ac_en, ac_en))
                    print('  WARN %s %s: name not found in %r -> english fallback %r'
                          % (locale, key, ac[key], fallback))
                    out[key] = fallback
        with open(os.path.join(PACK, 'lang', '%s.json' % locale), 'w', encoding='utf-8') as fh:
            json.dump(dict(sorted(out.items())), fh, ensure_ascii=False, indent=2)
            fh.write('\n')
        total += len(out)
        print('%s: %d overlay keys' % (locale, len(out)))

    # ---- guide book prose -------------------------------------------------------------------
    written = 0
    for name in jar.namelist():
        if not (name.startswith('assets/alexscaves/books/') and name.endswith('.txt')):
            continue
        rel = name[len('assets/alexscaves/books/'):]        # <locale>/<chapter>/<entry>.txt
        locale = rel.split('/')[0]
        try:
            ac = json.loads(jar.read('assets/alexscaves/lang/%s.json' % locale))
        except KeyError:
            ac = dict(ac_en)
        pairs = name_pairs(ac, ac_en)
        text = jar.read(name).decode('utf-8')
        new = substitute_book(locale, text, pairs)
        if locale == 'en_us':
            new = fix_english_articles(new)
        if new == text:
            continue
        out_path = os.path.join(PACK, 'books', rel)
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        # Upstream pages use CRLF; newline='' keeps regeneration byte-for-byte stable.
        with open(out_path, 'w', encoding='utf-8', newline='') as fh:
            fh.write(new)
        written += 1
    print('%d overlay lang keys, %d guide pages rewritten' % (total, written))


if __name__ == '__main__':
    main()
