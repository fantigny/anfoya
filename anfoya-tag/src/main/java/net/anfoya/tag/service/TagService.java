package net.anfoya.tag.service;

import java.util.Set;

import net.anfoya.tag.model.SpecialSection;
import net.anfoya.tag.model.SpecialTag;

public interface TagService<S extends Section, T extends Tag> {

	Set<S> getSections() throws TagException;
	long getCountForSection(S section, Set<T> includes, Set<T> excludes, String itemPattern) throws TagException;
	S getSpecialSection(SpecialSection section);

	S addSection(String name) throws TagException;
	void remove(S Section) throws TagException;
	S rename(S Section, String name) throws TagException;
	void hide(S Section) throws TagException;
	void show(S Section) throws TagException;

	T findTag(String name) throws TagException;
	T getTag(String id) throws TagException;
	Set<T> getTags(S section) throws TagException;
	Set<T> getTags(String pattern) throws TagException;
	long getCountForTags(Set<T> includes, Set<T> excludes, String pattern) throws TagException;

	Set<T> getHiddenTags() throws TagException;
	T getSpecialTag(SpecialTag specialTag) throws TagException;

	T addTag(String name) throws TagException;
	void remove(T tag) throws TagException;
	T rename(T tag, String name) throws TagException;
	void hide(T tag) throws TagException;
	void show(T tag) throws TagException;

	T moveToSection(T tag, S section) throws TagException;

	void addOnUpdateTagOrSection(Runnable callback);
}
