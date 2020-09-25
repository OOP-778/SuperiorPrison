package com.bgsoftware.superiorprison.plugin.test.script.math;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MathTester {
    private static final List<Character> mathChars = Arrays.asList(
            '-',
            '+',
            '^',
            '/',
            '*'
    );

    public static List<GroupsData> parse(String input) {
        char[] chars = input.toCharArray();

        List<GroupsData> matches = new ArrayList<>();

        // For matching groups
        boolean isInsideGroup = false;
        int depth = 0;

        GroupsData currentGroup = new GroupsData(0, new StringBuilder());
        currentGroup.setStart(-1);

        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];

            // Ignore space
            if (character == ' ') continue;

            if (character == '(') {
                depth++;

                if (!isInsideGroup && currentGroup.getStart() == -1)
                    currentGroup.setStart(i);

                GroupsData newGroup = new GroupsData(depth, new StringBuilder());
                newGroup.setStart(i);
                newGroup.parent = currentGroup;
                currentGroup.children = newGroup;

                currentGroup = newGroup;
                currentGroup.getBuilder().append(character);

                isInsideGroup = true;
                continue;
            }

            if (character == ')') {
                if (depth == 0) continue;
                depth--;
                currentGroup.getBuilder().append(character);

                if (depth == 0)
                    isInsideGroup = false;

                currentGroup.end = i;
                currentGroup = currentGroup.parent;
                continue;
            }

            // Check if character is a number
            if (Character.isDigit(character)) {
                OPair<String, Integer> nextThatMatches = getNextThatMatches(chars, i + 1, Character::isDigit);
                String foundNumString = nextThatMatches.getFirst();

                if (currentGroup.getStart() == -1)
                    currentGroup.setStart(i);

                currentGroup.getBuilder().append(character).append(foundNumString);
                i += nextThatMatches.getSecond();

                if (chars.length > i + 1 && chars[i + 1] == 'V')
                    i++;
                continue;
            }

            if (mathChars.contains(character)) {
                currentGroup.getBuilder().append(character);
                continue;
            }

            if (currentGroup.isReady()) {
                matches.add(currentGroup);
                currentGroup.setEnd(i);
                currentGroup = new GroupsData(0, new StringBuilder());
                currentGroup.setStart(-1);
                depth = 0;
                isInsideGroup = false;
            }
        }

        if (currentGroup.isReady()) {
            currentGroup.setEnd(input.length());
            matches.add(currentGroup);
        }

        return matches;
    }

    public static boolean validateMath(GroupsData groupsData) {
        List<GroupsData> children = groupsData.getChildrens();
        Collections.reverse(children);

        children.add(groupsData);
        ExpressionData lastData = null;

        for (GroupsData child : children) {
            char[] chars = child.getBuilder().toString().replace("(", "").replace(")", "").toCharArray();

            ExpressionData currentData = new ExpressionData();
            for (int i = 0; i < chars.length; i++) {
                char character = chars[i];

                // Check if character is a number
                if (Character.isDigit(character)) {
                    OPair<String, Integer> nextThatMatches = getNextThatMatches(chars, i + 1, Character::isDigit);
                    i += nextThatMatches.getSecond();

                    if (chars.length > i + 1 && chars[i + 1] == 'V')
                        i++;

                    if (!currentData.isNumberFound())
                        currentData.setNumberFound(true);
                    else
                        currentData.setSecondNumberFound(true);
                    continue;
                }

                if (mathChars.contains(character)) {
                    currentData.setModifierFound(true);
                    continue;
                }
            }

            // We got full expression
            if (currentData.isNumberFound() && currentData.isModifierFound())
                if (currentData.isSecondNumberFound())
                    lastData = currentData;

                else {
                    if (lastData != null && lastData.isFullyReady()) {
                        currentData.setSecondNumberFound(true);
                        lastData = currentData;
                    } else
                        return false;
                }
            else
                return false;
        }

        return true;
    }

    private static OPair<String, Integer> getNextThatMatches(char[] array, int startAt, Predicate<Character> filter) {
        StringBuilder builder = new StringBuilder();

        int indexPlus = 0;
        for (int i = startAt; i < array.length; i++) {

            char character = array[i];
            if (!filter.test(character))
                return new OPair<>(builder.toString(), indexPlus);

            indexPlus += 1;
            builder.append(character);
        }

        return new OPair<>(builder.toString(), indexPlus);
    }

    @RequiredArgsConstructor
    @Getter
    public static class GroupsData {
        private final int depth;
        private final StringBuilder builder;

        @Setter
        private GroupsData children;

        @Setter
        private GroupsData parent;

        @Setter
        private int start;

        @Setter
        private int end;

        public boolean isReady() {
            return wholeMatch(false).trim().length() > 0;
        }

        public String wholeMatch(boolean reversed) {
            List<String> list = new ArrayList<>();
            list.add(builder.toString());

            if (children != null)
                children.addToWholeMatch(list);

            if (reversed)
                Collections.reverse(list);

            return String.join("", list);
        }

        private void addToWholeMatch(List<String> match) {
            match.add(builder.toString());
            if (children != null)
                children.addToWholeMatch(match);
        }

        public List<GroupsData> getChildrens() {
            List<GroupsData> children = new ArrayList<>();
            if (this.children != null)
                this.children.addToChildrenList(children);

            return children;
        }

        private void addToChildrenList(List<GroupsData> childrens) {
            childrens.add(this);
            if (this.children != null)
                this.children.addToChildrenList(childrens);
        }

        @Override
        public String toString() {
            return "GroupsData{" +
                    "depth=" + depth +
                    ", builder=" + builder +
                    '}';
        }
    }

    @Getter
    @Setter
    private static class ExpressionData {
        private boolean numberFound = false;
        private boolean secondNumberFound = false;
        private boolean modifierFound = false;

        public boolean isFullyReady() {
            return numberFound && secondNumberFound && modifierFound;
        }

        public boolean isHalfReady() {
            return numberFound && secondNumberFound;
        }

        @Override
        public String toString() {
            return "ExpressionData{" +
                    "numberFound=" + numberFound +
                    ", secondNumberFound=" + secondNumberFound +
                    ", modifierFound=" + modifierFound +
                    '}';
        }
    }
}
