package com.example.deliveryteka.fragments.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.deliveryteka.R
import com.example.deliveryteka.activities.MainActivity
import com.example.deliveryteka.data.models.RequestAccess
import com.example.deliveryteka.data.viewmodel.MedicineListViewModel
import com.example.deliveryteka.databinding.FragmentLoginBinding
import com.example.deliveryteka.utility.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: MedicineListViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        val slots = UnderscoreDigitSlotsParser().parseSlots("+375 (__)__-__-___")
        val formatWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(slots)
        )
        formatWatcher.installOn(binding.phonesInput)

        binding.loginBtn.setOnClickListener {

            binding.progressBar.isVisible = true

            if (verifyDataFromUser(
                    binding.phonesInput.text.toString(),
                    binding.passwordsInput.text.toString()
                )
            ) {
                viewModel.login(
                    RequestAccess(
                        binding.phonesInput.text.toString(),
                        binding.passwordsInput.text.toString()
                    )

                )
                viewModel.login.observe(viewLifecycleOwner, { response ->
                    if (response[0].user_id.isNotEmpty()) {

                        val sharedPref = requireActivity().getSharedPreferences(
                            Constants.USER_ID,
                            Context.MODE_PRIVATE
                        )
                        sharedPref?.let {
                            with(sharedPref.edit()) {
                                putInt(Constants.USER_ID, response[0].user_id.toInt()).apply()
                            }
                        }



                        binding.progressBar.isVisible = false
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                    } else {

                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Вы неправильно ввели номер телефона или пароль!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })

            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun verifyDataFromUser(phone: String, password: String): Boolean {
        binding.progressBar.isVisible = false
        return if (phone.length != 18) {
            Toast.makeText(
                requireContext(),
                getString(R.string.enter_number_phone_correctly),
                Toast.LENGTH_LONG
            )
                .show()
            false
        } else if (phone.isNotEmpty() && password.isNotEmpty()) {
            true
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_all_fields_filled),
                Toast.LENGTH_LONG
            )
                .show()
            false
        }
    }
}