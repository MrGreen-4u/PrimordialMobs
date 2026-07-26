"""Propagate the five renamed mobs (Rammer / Logger / Roarer / Glider / Stealer) into every
language file, and rebuild the compat resource pack from the main lang files.

Why this exists
---------------
The mod's whole identity is that Alex's Caves' five Primordial Caves dinosaurs are renamed.
Until now only en_us carried the rename: every other locale still showed Alex's Caves' own
names (Temblorsaurio, Tremorozaur, 撼地龍 ...), both standalone and in compat mode.

What it writes, per locale (12 non-English locales x 5 mobs x 3 keys):
  entity.alexscaves.<mob>            -> the English mod name (proper noun, kept untranslated)
  block.alexscaves.<mob>_egg         -> Alex's Caves' own localized string with the localized
  item.alexscaves.spawn_egg_<mob>       creature name substituted by the English mod name, so
                                        the locale's own grammar/word order survives
                                        ("Huevo de Roarer", "Roarer Yumurtası", "Roarer蛋").
Russian/Ukrainian decline the creature name inside those strings; a Cyrillic case ending glued
to a Latin name reads wrong, so trailing lowercase Cyrillic is dropped. Two Alex's Caves strings
contain typos that break substring matching (pl_pl vallumraptor egg, zh_tw subterranodon spawn
egg) and are handled by MANUAL.

The compat pack (resourcepacks/primordial_compat, registered on top of every resource pack when
the full Alex's Caves is installed) is regenerated as a copy of the main lang files, minus the
two keys that name *Alex's Caves' own* UI in compat mode: its creative tab and its special-ability
keybind both belong to Alex's Caves there (we register neither), so those keys are restored to
Alex's Caves' own values instead of ours.

Run:  python3 tools/localize_names.py   (from the primordialmobs project dir)
"""
import json
import os
import re
import shutil
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
LANG = os.path.join(PROJ, 'src/main/resources/assets/alexscaves/lang')
PACK = os.path.join(PROJ, 'src/main/resources/resourcepacks/primordial_compat/assets/alexscaves/lang')
AC_JAR = os.path.join(PROJ, 'libs/alexscaves-full-2.0.2.jar')

MOBS = {'grottoceratops': 'Rammer', 'relicheirus': 'Logger', 'tremorsaurus': 'Roarer',
        'subterranodon': 'Glider', 'vallumraptor': 'Stealer'}

# Alex's Caves strings whose creature name is misspelled, so substring substitution misses it.
MANUAL = {
    ('pl_pl', 'block.alexscaves.vallumraptor_egg'): 'Jajo Stealera',
    ('zh_tw', 'item.alexscaves.spawn_egg_subterranodon'): 'Glider 生怪蛋',
}

# Keys that name Alex's Caves' own UI: ours standalone, theirs in compat mode.
AC_OWN_UI = ['itemGroup.alexscaves.primordial_caves', 'key.special_ability']

CYRILLIC_TAIL = re.compile(r'^[а-яёіїєґ]+')


def substitute(locale, ac_name, ac_value, english):
    """Replace the localized creature name inside an Alex's Caves string with the mod name."""
    if not ac_name or not ac_value:
        return None
    i = ac_value.lower().find(ac_name.lower())
    if i < 0:
        return None
    head, tail = ac_value[:i], ac_value[i + len(ac_name):]
    if locale in ('ru_ru', 'uk_ua'):
        tail = CYRILLIC_TAIL.sub('', tail)          # drop the case ending glued to a Latin name
    return head + english + tail


def main():
    ac_jar = zipfile.ZipFile(AC_JAR)

    def ac_lang(locale):
        return json.loads(ac_jar.read('assets/alexscaves/lang/%s.json' % locale))

    en = json.load(open(os.path.join(LANG, 'en_us.json'), encoding='utf-8'))
    locales = sorted(f[:-5] for f in os.listdir(LANG) if f.endswith('.json'))

    for locale in locales:
        path = os.path.join(LANG, '%s.json' % locale)
        data = json.load(open(path, encoding='utf-8'))
        if locale != 'en_us':
            ac = ac_lang(locale)
            changed = []
            for mob, english in MOBS.items():
                ac_name = ac.get('entity.alexscaves.%s' % mob)
                for key in ('entity.alexscaves.%s' % mob,
                            'block.alexscaves.%s_egg' % mob,
                            'item.alexscaves.spawn_egg_%s' % mob):
                    if key not in data:
                        continue
                    if key == 'entity.alexscaves.%s' % mob:
                        value = english
                    elif (locale, key) in MANUAL:
                        value = MANUAL[(locale, key)]
                    else:
                        value = substitute(locale, ac_name, ac.get(key), english) or en[key]
                    if data[key] != value:
                        changed.append('%s: %r -> %r' % (key, data[key], value))
                        data[key] = value
            if changed:
                with open(path, 'w', encoding='utf-8') as fh:
                    json.dump(data, fh, ensure_ascii=False, indent=2)
                    fh.write('\n')
            print('%s: %d name keys updated' % (locale, len(changed)))

        # compat pack copy: same names, but Alex's Caves keeps its own tab/keybind labels
        compat = dict(json.load(open(path, encoding='utf-8')))
        ac = ac_lang(locale)
        ac_en = ac_lang('en_us')
        for key in AC_OWN_UI:
            if key in compat:
                theirs = ac.get(key, ac_en.get(key))
                if theirs is not None:
                    compat[key] = theirs
        with open(os.path.join(PACK, '%s.json' % locale), 'w', encoding='utf-8') as fh:
            json.dump(compat, fh, ensure_ascii=False, indent=2)
            fh.write('\n')

    print('compat pack rebuilt from %d lang files' % len(locales))


if __name__ == '__main__':
    main()
