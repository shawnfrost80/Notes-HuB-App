package com.example.lakshayanoteshub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {
    String string;
    Map<String, List<Note>> map;

    public Converter(String string) {
        this.string = string;
    }

    public Converter(Map<String, List<Note>> map) {
        this.map = map;
    }

    String convertToString() {
        String result = "";
        for (String s : map.keySet()) {
            result = result + s + "~";
            for (Note note : map.get(s)) {
                result += note.id + "~" + note.name + "~" + note.subjectName + "~" + note.description + "~" + note.url + "~" + note.pdfname + "~" + note.email + "~" + note.size + "~" + note.addedOn + "^";
            }
            result += ",";
        }

        return result;
    }

    Map<String, List<Note>> convertToMap() {
        Map<String, List<Note>> m = new HashMap<>();
        char[] chars = string.toCharArray();
        int i = 0;
        String key = "";
        List<Note> list = new ArrayList<>();
        String id = "", name = "", subjectName = "", description = "", url = "", pdfname = "", email = "", size = "", addedOn = "";
//        List<String> noteValue = new ArrayList<>(
//                Arrays.asList("", "", "", "", "", "", "", "", ""));
        for (char c : chars) {
            if (i == 0 && c != '~') {
                key += c;
            } else if (c == '~') {
                i++;
            }

            if (i != 0 && c != '~' && c != '^') {
                if (i == 1) id += c;
                else if (i == 2) name += c;
                else if (i == 3) subjectName += c;
                else if (i == 4) description += c;
                else if (i == 5) url += c;
                else if (i == 6) pdfname += c;
                else if (i == 7) email += c;
                else if (i == 8) size += c;
                else if (i == 9) addedOn += c;
//                noteValue.add(i, noteValue.get(i)+c);
            } else if (c == '^') {
                Note note = new Note(id, name, subjectName, description, url, pdfname, email, size, addedOn);
//                Note note = new Note(noteValue.get(1), noteValue.get(2), noteValue.get(3), noteValue.get(4), noteValue.get(5), noteValue.get(6), noteValue.get(7), noteValue.get(8));
                list.add(note);
//                noteValue = new ArrayList<>(
//                        Arrays.asList("", "", "", "", "", "", "", "", ""));
                id = "";
                name = "";
                subjectName = "";
                description = "";
                url = "";
                pdfname = "";
                email = "";
                size = "";
                i = 1;
            }

            if (c == ',') {
                m.put(key, list);
                list = new ArrayList<>();
                key = "";
                i = 0;
            }
        }

        return m;
    }

}
