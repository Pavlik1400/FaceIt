"""
Module for UsersData datastuct
"""


class UsersData:
    """
    Class - datastruct for users in database
    """
    def __init__(self, name, photo_encoded, right_level, passwd):
        self.__name = name
        self.__photo_encoded = photo_encoded
        self.__right_level = right_level
        self.__passwd = passwd

    def get_photo(self):
        """
        Returns encoded biometrical info of someone`s face
        """
        return self.__photo_encoded

    def return_name(self):
        """
        Returns user`s name
        """
        return self.__name

    def return_right_level(self):
        """
        Returns user`s level of right
        """
        return self.__right_level

    def return_passwd(self):
        """
        Returns user`s recovery password
        """
        return self.__passwd
