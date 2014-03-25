Sequel.migration do
  up do
    create_table(:people) do
      primary_key :id

      String :first_name, :size => 200
      String :last_name, :size => 200
      String :email, :size => 200

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:people)
  end
end
