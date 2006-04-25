#ifndef TOKEN_H
#define TOKEN_H

template <class T> class Token {
public: 
    Token (T value);
	void setValue (T value); //make this hidden, only avi, to friend class. Joh.
	T getValue(void);
private:
	T _value;
};

template <class T> Token<T>::Token (T value) {_value = value;}

template <class T> void Token<T>::setValue(T value) {
	_value = value;
}

template <class T> T Token<T>::getValue(void) {
	return _value;
}

typedef Token<int> INTTOKEN;
#endif 

